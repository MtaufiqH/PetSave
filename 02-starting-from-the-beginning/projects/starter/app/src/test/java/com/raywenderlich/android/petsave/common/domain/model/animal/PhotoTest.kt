package com.raywenderlich.android.petsave.common.domain.model.animal

import org.junit.Assert.assertEquals
import org.junit.Test

internal class PhotoTest {

    private val mediumPhoto = "medium"
    private val fullPhoto = "full"
    private val invalidPhoto = ""

    @Test
    fun photo_getSmallestPhoto_hasMediumPhoto() {
        // GIVEN
        val photo = Media.Photo(mediumPhoto, fullPhoto)
        val expectedPhoto = mediumPhoto

        // when
        val smallestPhoto = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(expectedPhoto, smallestPhoto)
    }

    @Test
    fun photo_getSmallestPhoto_noMediumPhoto() {
        val photo = Media.Photo(invalidPhoto, fullPhoto)
        val expectedPhoto = fullPhoto

        // when
        val getSmallestPhoto = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(expectedPhoto, getSmallestPhoto)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_invalidPhoto() {
        val photo = Media.Photo(invalidPhoto, invalidPhoto)
        val expectedPhoto = Media.Photo.EMPTY_PHOTO

        val getSmallestPhoto = photo.getSmallestAvailablePhoto()

        // THEN
        assertEquals(expectedPhoto, getSmallestPhoto)
    }
}