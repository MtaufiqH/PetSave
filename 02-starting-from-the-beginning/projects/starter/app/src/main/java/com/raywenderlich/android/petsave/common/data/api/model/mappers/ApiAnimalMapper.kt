package com.raywenderlich.android.petsave.common.data.api.model.mappers

import com.raywenderlich.android.petsave.common.data.api.model.ApiAnimal
import com.raywenderlich.android.petsave.common.domain.model.animal.AdoptionStatus
import com.raywenderlich.android.petsave.common.domain.model.animal.Media
import com.raywenderlich.android.petsave.common.domain.model.animal.details.*
import com.raywenderlich.android.petsave.common.domain.model.organization.Organization
import com.raywenderlich.android.petsave.common.utils.DateTimeUtils.parse
import java.util.*
import javax.inject.Inject

class ApiAnimalMapper @Inject constructor(
    private val apiBreedsMapper: ApiBreedsMapper,
    private val apiColorsMapper: ApiColorsMapper,
    private val apiHealthDetailsMapper: ApiHealthDetailsMapper,
    private val apiHabitatAdaptationMapper: ApiHabitatAdaptationMapper,
    private val apiPhotoMapper: ApiPhotoMapper,
    private val apiVideoMapper: ApiVideoMapper,
    private val apiContactMapper: ApiContactMapper
) : ApiMapper<ApiAnimal, AnimalDetails> {
    override fun mapToDomain(apiEntity: ApiAnimal): AnimalDetails {
        return AnimalDetails(
            id = apiEntity.id ?: throw MappingException("Animal ID cannot be null"),
            name = apiEntity.name.orEmpty(),
            type = apiEntity.type.orEmpty(),
            details = parseAnimalDetail(apiAnimal = apiEntity),
            media = mapMedia(apiEntity),
            tags = apiEntity.tags.orEmpty().map { it.orEmpty() },
            adoptionStatus = parseAdoptionStatus(apiEntity.status),
            publishedAt = parse(apiEntity.publishedAt.orEmpty())

        )
    }

    private fun mapMedia(apiEntity: ApiAnimal): Media {
        return Media(
            photos = apiEntity.photos?.map { apiPhotoMapper.mapToDomain(it) }.orEmpty(),
            videos = apiEntity.videos?.map { video -> apiVideoMapper.mapToDomain(video) }.orEmpty()
        )
    }


    private fun parseAdoptionStatus(status: String?): AdoptionStatus {
        if (status.isNullOrEmpty()) return AdoptionStatus.UNKNOWN

        return AdoptionStatus.valueOf(status.toUpperCase(Locale.ROOT))
    }

    private fun parseAnimalDetail(apiAnimal: ApiAnimal): Details {
        return Details(
            description = apiAnimal.description.orEmpty(),
            age = parseAge(apiAnimal.age),
            breed = apiBreedsMapper.mapToDomain(apiAnimal.breeds),
            species = apiAnimal.species.orEmpty(),
            colors = apiColorsMapper.mapToDomain(apiAnimal.colors),
            gender = parseGender(apiAnimal.gender),
            size = parseSize(apiAnimal.size),
            coat = parseCoat(apiAnimal.coat),
            healthDetails = apiHealthDetailsMapper.mapToDomain(apiAnimal.attributes),
            habitatAdaptation = apiHabitatAdaptationMapper.mapToDomain(apiAnimal.environment),
            organization = mapOrganization(apiAnimal)

        )

    }

    private fun mapOrganization(apiAnimal: ApiAnimal): Organization {
        return Organization(
            id = apiAnimal.organizationId
                ?: throw MappingException("organization id cannot be null"),
            contact = apiContactMapper.mapToDomain(apiAnimal.contact),
            distance = apiAnimal.distance ?: -1f
        )
    }

    private fun parseCoat(coat: String?): Coat {
        return if (coat.isNullOrEmpty()) Coat.UNKNOWN else
            Coat.valueOf(coat.toUpperCase(Locale.ROOT))
    }

    private fun parseSize(size: String?): Size {
        return if (size.isNullOrEmpty()) Size.UNKNOWN else
            Size.valueOf(size.toUpperCase(Locale.ROOT))

    }

    private fun parseGender(gender: String?): Gender {
        return if (gender.isNullOrEmpty()) Gender.UNKNOWN else Gender.valueOf(
            gender.toUpperCase(
                Locale.ROOT
            )
        )
    }

    private fun parseAge(age: String?): Age {
        return if (age.isNullOrEmpty()) Age.UNKNOWN else
            Age.valueOf(age.toUpperCase(Locale.ROOT))
    }
}