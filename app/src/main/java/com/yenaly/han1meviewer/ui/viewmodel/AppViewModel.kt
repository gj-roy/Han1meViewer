package com.yenaly.han1meviewer.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.worker.HUpdateWorker
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.base.YenalyViewModel
import com.yenaly.yenaly_libs.utils.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/29 029 18:00
 */
object AppViewModel : YenalyViewModel(application) {

    private val _versionFlow = MutableStateFlow<WebsiteState<Latest?>>(WebsiteState.Loading)
    val versionFlow = _versionFlow.asStateFlow()

    init {
        // 取消，防止每次启动都有残留的更新任务
        WorkManager.getInstance(application).pruneWork()

        viewModelScope.launch(Dispatchers.Main) {
            HUpdateWorker.collectOutput(application)
        }

        viewModelScope.launch(Dispatchers.Main) {
            HanimeDownloadWorker.collectOutput(application)
        }

        viewModelScope.launch {
            // 不要太提前
            delay(500)
            getLatestVersionSuspend()
        }
    }

    fun getLatestVersion(forceCheck: Boolean = true) {
        viewModelScope.launch {
            getLatestVersionSuspend(forceCheck)
        }
    }

    private suspend fun getLatestVersionSuspend(forceCheck: Boolean = true) {
        NetworkRepo.getLatestVersion(forceCheck).collect {
            _versionFlow.value = it
        }
    }
}