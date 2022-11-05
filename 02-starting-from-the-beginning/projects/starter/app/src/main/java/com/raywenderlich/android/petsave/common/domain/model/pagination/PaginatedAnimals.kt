package com.raywenderlich.android.petsave.common.domain.model.pagination

import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.animal.details.AnimalDetails

data class PaginatedAnimals(
    val animal: List<AnimalDetails>,
    val pagination: Pagination
)