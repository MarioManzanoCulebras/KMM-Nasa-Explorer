import com.mariomanzano.kmm_nasa_explorer.di.getBaseModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(additionalModules: List<Module>) {
    startKoin {
        modules(additionalModules + getBaseModules())
    }
}