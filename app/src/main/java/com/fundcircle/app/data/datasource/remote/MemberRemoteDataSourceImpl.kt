package com.fundcircle.app.data.datasource.remote

import com.fundcircle.app.data.dto.CircleMemberDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
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
                }.orEmpty()
                trySend(members)
            }

        awaitClose {
            registration.remove()
        }
    }

    companion object {
        private const val COLLECTION_FUNDS = "funds"
        private const val COLLECTION_MEMBERS = "members"
    }
}
