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

import com.babylon.orbit2.Stream
import rx.Observable

/**
 * Consume a [Stream] as an RxJava 1 [Observable].
 */
/*fun <T> Stream<T>.asRx1Observable() =
    Observable.create<T> { emitter ->
        val closeable = observe {
            if (!emitter.isDisposed) {
                emitter.onNext(it)
            }
        }
        emitter.setCancellable { closeable.close() }
    }
*/
