package com.schoolerz.core.util

import org.junit.Assert.*
import org.junit.Test

class EmailValidationTest {

    // List of approved school domains
    private val approvedDomains = listOf(
        "student.edu",
        "k12.ca.us",
        "lausd.net",
        "sfusd.edu",
        "mvla.net",
        "pausd.org",
        "berkeley.net"
    )

    @Test
    fun `valid school email returns true`() {
        assertTrue(isValidSchoolEmail("john@student.edu"))
        assertTrue(isValidSchoolEmail("jane.doe@k12.ca.us"))
        assertTrue(isValidSchoolEmail("student123@lausd.net"))
    }

    @Test
    fun `invalid domain returns false`() {
        assertFalse(isValidSchoolEmail("john@gmail.com"))
        assertFalse(isValidSchoolEmail("jane@yahoo.com"))
        assertFalse(isValidSchoolEmail("student@hotmail.com"))
    }

    @Test
    fun `empty email returns false`() {
        assertFalse(isValidSchoolEmail(""))
        assertFalse(isValidSchoolEmail("   "))
    }

    @Test
    fun `email without @ returns false`() {
        assertFalse(isValidSchoolEmail("johngmail.com"))
        assertFalse(isValidSchoolEmail("noemail"))
    }

    @Test
    fun `email extraction works correctly`() {
        val domain = extractDomain("john@student.edu")
        assertEquals("student.edu", domain)
    }

    @Test
    fun `domain extraction handles subdomain`() {
        val domain = extractDomain("user@mail.student.edu")
        assertEquals("mail.student.edu", domain)
    }

    @Test
    fun `null or blank email handling`() {
        assertFalse(isValidSchoolEmail(null))
        assertFalse(isValidSchoolEmail(""))
    }

    @Test
    fun `case insensitive domain matching`() {
        assertTrue(isValidSchoolEmail("john@STUDENT.EDU"))
        assertTrue(isValidSchoolEmail("john@Student.Edu"))
        assertTrue(isValidSchoolEmail("john@K12.CA.US"))
    }

    @Test
    fun `subdomain of approved domain is valid`() {
        assertTrue(isValidSchoolEmail("john@mail.student.edu"))
        assertTrue(isValidSchoolEmail("john@students.k12.ca.us"))
    }

    @Test
    fun `multiple @ symbols returns false`() {
        assertFalse(isValidSchoolEmail("john@@student.edu"))
        assertFalse(isValidSchoolEmail("john@test@student.edu"))
    }

    @Test
    fun `email with special characters in local part`() {
        assertTrue(isValidSchoolEmail("john.doe+tag@student.edu"))
        assertTrue(isValidSchoolEmail("john_doe@student.edu"))
    }

    @Test
    fun `similar but not approved domain returns false`() {
        assertFalse(isValidSchoolEmail("john@studentedu.com"))
        assertFalse(isValidSchoolEmail("john@fakestudent.edu"))
    }

    @Test
    fun `all approved domains work`() {
        approvedDomains.forEach { domain ->
            assertTrue("Domain $domain should be valid", isValidSchoolEmail("test@$domain"))
        }
    }

    @Test
    fun `extract domain handles edge cases`() {
        assertEquals("domain.com", extractDomain("user@domain.com"))
        assertEquals("sub.domain.com", extractDomain("user@sub.domain.com"))
        assertEquals("", extractDomain("user@"))
    }

    private fun isValidSchoolEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        if (!email.contains("@")) return false

        val domain = extractDomain(email)
        return approvedDomains.any { approved ->
            domain == approved || domain.endsWith(".$approved")
        }
    }

    private fun extractDomain(email: String): String {
        return email.substringAfter("@").lowercase()
    }
}
