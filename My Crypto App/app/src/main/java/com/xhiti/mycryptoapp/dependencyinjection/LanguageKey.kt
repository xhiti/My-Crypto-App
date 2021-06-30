package com.baruckis.kriptofolio.dependencyinjection

import dagger.MapKey

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class LanguageKey(val value: Language)