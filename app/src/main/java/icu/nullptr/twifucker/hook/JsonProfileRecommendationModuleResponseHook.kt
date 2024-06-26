package icu.nullptr.twifucker.hook

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.finders.FieldFinder
import com.github.kyuubiran.ezxhelper.finders.MethodFinder
import icu.nullptr.twifucker.afterMeasure
import icu.nullptr.twifucker.modulePrefs

object JsonProfileRecommendationModuleResponseHook : BaseHook() {
    override val name: String
        get() = "ProfileRecommendationModule"

    override fun init() {
        if (!modulePrefs.getBoolean("disable_recommended_users", false)) return

        val jsonProfileRecommendationModuleResponseClass =
            loadClass("com.twitter.model.json.people.JsonProfileRecommendationModuleResponse")
        val jsonProfileRecommendationModuleResponseMapperClass =
            loadClass("com.twitter.model.json.people.JsonProfileRecommendationModuleResponse\$\$JsonObjectMapper")


        val recommendedUsersField =
            FieldFinder.fromClass(jsonProfileRecommendationModuleResponseClass)
                .filterByType(ArrayList::class.java).first()

        MethodFinder.fromClass(jsonProfileRecommendationModuleResponseMapperClass)
            .filterByName("parse").filterByReturnType(jsonProfileRecommendationModuleResponseClass)
            .first().createHook {
                afterMeasure(name) { param ->
                    param.result ?: return@afterMeasure
                    recommendedUsersField.get(param.result).let { users ->
                        if (users is ArrayList<*> && users.isNotEmpty()) {
                            recommendedUsersField.set(param.result, null)
                            Log.d("Removed recommended users")
                        }
                    }
                }
            }
    }
}