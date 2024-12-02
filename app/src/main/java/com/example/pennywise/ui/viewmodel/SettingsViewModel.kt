import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.local.entities.AppSettingsEntity
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: PennyWiseRepository) : ViewModel() {

    fun saveSettings(isDarkMode: Boolean, preferredCurrency: String) = viewModelScope.launch {
        repository.saveSettings(AppSettingsEntity(isDarkMode = isDarkMode, preferredCurrency = preferredCurrency))
    }

    fun getSettings(): LiveData<AppSettingsEntity?> = liveData {
        emit(repository.getSettings())
    }


}
