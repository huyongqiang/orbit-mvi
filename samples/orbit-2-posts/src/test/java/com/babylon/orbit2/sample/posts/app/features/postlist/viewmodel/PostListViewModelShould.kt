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

package com.babylon.orbit2.sample.posts.app.features.postlist.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.babylon.orbit2.livedata.state
import com.babylon.orbit2.sample.posts.InstantTaskExecutorExtension
import com.babylon.orbit2.sample.posts.domain.repositories.PostOverview
import com.babylon.orbit2.sample.posts.domain.repositories.PostRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExtendWith(InstantTaskExecutorExtension::class)
class PostListViewModelShould {

    private val repository = mock<PostRepository>()

    private fun mockRepository() {
        runBlocking {
            whenever(repository.getOverviews())
                .then { listOf(PostOverview(1, "url", "title", "name", 1)) }
        }
    }

    @Test
    fun `request post overviews from repository`() {
        runBlocking {
            // given we mock the repository
            mockRepository()

            // when we initialise the view model and wait
            PostListViewModel(SavedStateHandle(), repository).container.stateStream.observe { }
            sleep(100)

            // then the post details are loaded from the repository
            verify(repository, times(1)).getOverviews()
        }
    }

    @Test
    fun `return post overviews from repository`() {
        val latch = CountDownLatch(1)

        // given we mock the repository
        mockRepository()

        // when we observe details from the view model
        PostListViewModel(SavedStateHandle(), repository).container.state.observeForever {
            if (it.overviews.isNotEmpty()) {
                latch.countDown()
            }
        }

        // then the data is posted, and the latch counts down
        assertTrue(latch.await(250, TimeUnit.MILLISECONDS))
    }
}