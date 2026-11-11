package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Product
import com.example.data.repository.ShopRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ShopRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addProduct_validPayload_createsDocument() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ShopRepository(firestore)

    val product = Product(
      userId = uid,
      name = "Earphone Basic",
      category = "Earphones",
      brand = "Realme",
      purchasePrice = 120.0,
      sellingPrice = 200.0,
      currentStock = 10L,
      minStockAlert = 2L
    )

    val prodId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addProduct(uid, product)
    }
    assertTrue(prodId.isNotEmpty())
  }

  @Test
  fun observeProducts_authenticatedOwner_returnsProducts() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = ShopRepository(firestore)

    val uniqueName = "Product-${UUID.randomUUID().toString().take(6)}"
    val product = Product(
      userId = uid,
      name = uniqueName,
      category = "Tempered Glass",
      purchasePrice = 30.0,
      sellingPrice = 80.0,
      currentStock = 50L
    )
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addProduct(uid, product)
    }

    val products = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeProducts(uid).first { list -> list.any { it.name == uniqueName } }
    }
    assertTrue(products.any { it.name == uniqueName })
  }

  @Test
  fun crossUserAccess_bobCannotAccessAliceProducts() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = ShopRepository(firestore)
    val prodId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.addProduct(
        aliceUid,
        Product(userId = aliceUid, name = "Alice Phone", category = "New Mobile Phones", purchasePrice = 15000.0, sellingPrice = 17000.0, currentStock = 2L)
      )
    }

    val bobUid = signInTestUser(BOB_EMAIL)
    val bobRepo = ShopRepository(firestore)

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        bobRepo.deleteProduct(aliceUid, prodId)
      }
      org.junit.Assert.fail("Expected PERMISSION_DENIED on cross-user delete")
    } catch (e: Exception) {
      val fe = e.unwrapFirestoreException()
      assertNotNull(fe)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fe?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@yasin-telecom.com"
    const val BOB_EMAIL = "bob@other-shop.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
