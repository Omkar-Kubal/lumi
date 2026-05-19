package com.appylab.lumi.ui.viewmodel

sealed class AppStartState {
    object Loading         : AppStartState()
    object NeedsOnboarding : AppStartState()
    object NeedsLogin      : AppStartState()
    object Ready           : AppStartState()
}
