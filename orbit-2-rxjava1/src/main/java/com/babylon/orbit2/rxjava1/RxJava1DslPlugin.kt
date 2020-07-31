/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.rxjava1

import com.babylon.orbit2.Context
import com.babylon.orbit2.Operator
import com.babylon.orbit2.OrbitDslPlugin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import rx.Observable
import rx.Observer
import rx.Single
import rx.SingleSubscriber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Orbit plugin providing RxJava 1 DSL operators:
 *
 * * [transformRx1Observable]
 * * [transformRx1Single]
 * * [transformRx1Completable]
 */
object RxJava1DslPlugin : OrbitDslPlugin {

    @Suppress("UNCHECKED_CAST", "EXPERIMENTAL_API_USAGE")
    override fun <S : Any, E : Any, SE : Any> apply(
        containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> Context<S, E>
    ): Flow<Any> {
        return when (operator) {
            is RxJava1Observable<*, *, *> -> flow.flatMapConcat {
                with(operator as RxJava1Observable<S, E, Any>) {
                    createContext(it).block()
                }.asFlow().flowOn(containerContext.backgroundDispatcher)
            }
            is RxJava1Single<*, *, *> -> flow.map {
                with(operator as RxJava1Single<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block().await()
                    }
                }
            }
            is RxJava1Completable -> flow.onEach {
                with(operator) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block().toSingle {}.await()
                    }
                }
            }
            else -> flow
        }
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
private fun <T : Any> Observable<T>.asFlow(): Flow<T> = kotlinx.coroutines.flow.callbackFlow {
    val observer = object : Observer<T> {
        override fun onError(e: Throwable?) {
            close(e)
        }

        override fun onNext(t: T) {
            sendBlocking(t)
        }

        override fun onCompleted() {
            close()
        }
    }

    val subscription = subscribe(observer)
    awaitClose { subscription.unsubscribe() }
}

private suspend fun <T> Single<T>.await(): T = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
    val subscription = subscribe(object : SingleSubscriber<T>() {
        override fun onSuccess(t: T) {
            cont.resume(t)
        }

        override fun onError(error: Throwable) {
            cont.resumeWithException(error)
        }
    })
    cont.invokeOnCancellation { subscription.unsubscribe() }
}
