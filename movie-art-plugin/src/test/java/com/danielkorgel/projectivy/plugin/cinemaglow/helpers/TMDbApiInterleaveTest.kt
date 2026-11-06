package com.danielkorgel.projectivy.plugin.cinemaglow.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

class TMDbApiInterleaveTest {

    @Test
    fun alternatesTheTwoLists() {
        assertEquals(listOf("m1", "s1", "m2", "s2"), TMDbApi.interleave(listOf("m1", "m2"), listOf("s1", "s2")))
    }

    @Test
    fun keepsTheRestOfTheLongerList() {
        assertEquals(listOf("m1", "s1", "s2", "s3"), TMDbApi.interleave(listOf("m1"), listOf("s1", "s2", "s3")))
        assertEquals(listOf("m1", "m2"), TMDbApi.interleave(listOf("m1", "m2"), emptyList()))
    }

    @Test
    fun twoEmptyListsGiveAnEmptyList() {
        assertEquals(emptyList<String>(), TMDbApi.interleave(emptyList<String>(), emptyList()))
    }
}
