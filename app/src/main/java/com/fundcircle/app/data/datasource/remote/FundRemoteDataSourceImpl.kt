package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.FundCircleDto
import com.fundcircle.app.data.dto.MemberDto
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
class FundRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FundRemoteDataSource {

    override fun observeUserCircles(userEmail: String): Flow<List<FundCircleDto>> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .whereArrayContains(FIELD_MEMBER_EMAILS, userEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val circles = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FundCircleDto::class.java)?.let { dto ->
                        if (dto.fundId.isBlank()) dto.copy(fundId = doc.id) else dto
                    }
                }.orEmpty()
                trySend(circles)
            }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeCircle(fundId: String): Flow<FundCircleDto?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val circle = snapshot?.toObject(FundCircleDto::class.java)?.let { dto ->
                    if (dto.fundId.isBlank()) dto.copy(fundId = snapshot.id) else dto
                }
                trySend(circle)
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun createCircle(
        fundCircle: FundCircleDto,
        adminMember: MemberDto
    ): Result<Unit> = try {
        val batch = firestore.batch()
        val fundRef = firestore.collection(COLLECTION_FUNDS).document(fundCircle.fundId)
        val memberRef = fundRef.collection(COLLECTION_MEMBERS).document(adminMember.userId)
        val userRef = firestore.collection(COLLECTION_USERS).document(adminMember.userId)

        batch.set(fundRef, fundCircle)
        batch.set(memberRef, adminMember)
        batch.set(userRef, mapOf(FIELD_CREATED_CIRCLE_COUNT to FieldValue.increment(1)), SetOptions.merge())

        batch.commit().await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    override suspend fun toggleCircleLock(fundId: String, isLocked: Boolean): Result<Unit> = try {
        firestore.collection(COLLECTION_FUNDS)
            .document(fundId)
            .update(
                mapOf(
                    FIELD_IS_LOCKED to isLocked,
                    FIELD_UPDATED_AT to System.currentTimeMillis()
                )
            ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    companion object {
        private const val COLLECTION_FUNDS = "funds"
        private const val COLLECTION_MEMBERS = "members"
        private const val COLLECTION_USERS = "users"
        private const val FIELD_MEMBER_EMAILS = "memberEmails"
        private const val FIELD_IS_LOCKED = "isLocked"
        private const val FIELD_UPDATED_AT = "updatedAt"
        private const val FIELD_CREATED_CIRCLE_COUNT = "createdCircleCount"
    }
}
