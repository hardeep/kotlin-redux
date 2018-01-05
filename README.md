# Redux for Kotlin

[![Release](https://jitpack.io/v/hardeep/kotlin-redux.svg)](https://jitpack.io/#hardeep/kotlin-redux)

## Example usage

    import ca.hardeep.kotlin.redux.*

    fun main(args: Array<String>) {

        class TestAction: ActionType
        class AnotherTestAction: ActionType

        data class ApplicationState(
            val strings: List<String> = listOf()
        )

        val initialState = ApplicationState()

        val firstReducer = fun(state: ApplicationState, action: Action): ApplicationState {
            return when (action.type::class) {
                TestAction::class -> {
                    val newStrings = state.strings.toMutableList()
                    newStrings.add("Adding first string")
                    return state.copy(strings = newStrings.toList())
                }
                else -> {
                    state
                }
            }
        }

        val secondReducer = fun(state: ApplicationState, action: Action): ApplicationState {
            return when (action.type::class) {
                AnotherTestAction::class -> {
                    val newStrings = state.strings.toMutableList()
                    newStrings.add("Adding another string")
                    return state.copy(strings = newStrings.toList())
                }
                else -> {
                    state
                }
            }
        }

        val combined = combineReducers<ApplicationState>(firstReducer, secondReducer);

        val store = createStore(combined, initialState);

        store.dispatch(Action(TestAction()))
        store.dispatch(Action(AnotherTestAction()))

        println(store.getState())
    }

    // Result: ApplicationState(strings=[Adding first string, Adding another string])
