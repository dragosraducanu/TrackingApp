package com.dragos.challenge.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class File(val type: FileType) {
    enum class FileType {
        CACHE
    }
}