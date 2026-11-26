package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.core.network.NetworkMonitor
import com.fundcircle.app.data.dto.CircleMemberDto
import com.fundcircle.app.data.dto.MemberKycDto
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val networkMonitor: NetworkMonitor
) : MemberRemoteDataSource {

    override fun observeMembers(fundId: String): Flow<List<CircleMemberDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_MEMBERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CircleMemberDto::class.java)?.let { dto ->
                        dto.copy(
                            userId = if (dto.userId.isBlank()) doc.id else dto.userId
                        )
                    }
                }.orEmpty().sortedBy { it.memberId }
                trySend(members)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeMember(fundId: String, userId: String): Flow<CircleMemberDto?> = callbackFlow {
        val memberDocRef = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_MEMBERS)
            .document(userId)

        val registration = memberDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val dto = snapshot.toObject(CircleMemberDto::class.java)?.let {
                    it.copy(userId = if (it.userId.isBlank()) snapshot.id else it.userId)
                }
                trySend(dto)
            } else {
                // Fallback check by sequential memberId (e.g. "FC-002")
                firestore.collection(COLLECTION_FUNDS)
                    .document(fundId)
                    .collection(COLLECTION_MEMBERS)
                    .whereEqualTo("memberId", userId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val found = querySnapshot.documents.firstOrNull()?.let { doc ->
                            doc.toObject(CircleMemberDto::class.java)?.let {
                                it.copy(userId = if (it.userId.isBlank()) doc.id else it.userId)
                            }
                        }
                        trySend(found)
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun getMemberKyc(fundId: String, userId: String): Result<MemberKycDto> = try {
        val snapshot = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_MEMBERS)
            .document(userId)
            .collection(COLLECTION_KYC)
            .document(DOCUMENT_IDENTITY)
            .get()
            .await()

        val kycDto = if (snapshot.exists()) {
            snapshot.toObject(MemberKycDto::class.java)?.copy(
                userId = if (snapshot.getString("userId").isNullOrBlank()) userId else snapshot.getString("userId") ?: userId
            ) ?: MemberKycDto(userId = userId)
        } else {
            MemberKycDto(userId = userId)
        }
        Result.success(kycDto)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun updateMemberKyc(fundId: String, kyc: MemberKycDto): Result<Unit> = try {
        firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_MEMBERS)
            .document(kyc.userId)
            .collection(COLLECTION_KYC)
            .document(DOCUMENT_IDENTITY)
            .set(kyc, SetOptions.merge())
            .awaitSyncOrQueue(networkMonitor)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun inviteMember(fundId: String, member: CircleMemberDto): Result<Unit> = try {
        val batch = firestore.batch()
        val fundRef = firestore.collection(COLLECTION_FUNDS).document(fundId)
        val memberRef = fundRef.collection(COLLECTION_MEMBERS).document(member.userId)

        batch.set(memberRef, member)
        batch.update(
            fundRef,
            mapOf(
                FIELD_MEMBER_COUNT to FieldValue.increment(1),
                FIELD_MEMBER_EMAILS to FieldValue.arrayUnion(member.email),
                FIELD_UPDATED_AT to System.currentTimeMillis()
            )
        )

        batch.commit().awaitSyncOrQueue(networkMonitor)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun toggleMemberStatus(
        fundId: String,
        userId: String,
        status: String
    ): Result<Unit> = try {
        val memberRef = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .collection(COLLECTION_MEMBERS)
            .document(userId)

        memberRef.update(
            mapOf(
                FIELD_STATUS to status,
                FIELD_LAST_ACTIVE_AT to System.currentTimeMillis()
            )
        ).awaitSyncOrQueue(networkMonitor)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    companion object {
        private const val COLLECTION_FUNDS = "funds"
        private const val COLLECTION_MEMBERS = "members"
        private const val COLLECTION_KYC = "kyc"
        private const val DOCUMENT_IDENTITY = "identity"

        private const val FIELD_MEMBER_COUNT = "memberCount"
        private const val FIELD_MEMBER_EMAILS = "memberEmails"
        private const val FIELD_UPDATED_AT = "updatedAt"
        private const val FIELD_STATUS = "status"
        private const val FIELD_LAST_ACTIVE_AT = "lastActiveAt"
    }
}
