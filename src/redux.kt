package ca.hardeep.kotlin.redux

interface ActionType
typealias Reducer<State> = (state: State, action: Action) -> State
typealias Dispatcher = (action: Action) -> Action
typealias StoreCreator<State> = (reducer: Reducer<State>, preloadedState: State) -> Store<State>
typealias Next = ((action: Action) -> Action)
typealias NextMiddleware = (next: Next) -> Next
typealias Middleware<State> = (store: Store<State>) -> NextMiddleware

class Init: ActionType
data class Action(val type: ActionType, val payload: Any? = null)

fun <State> combineReducers(vararg reducers: Reducer<State>): Reducer<State> {
    return fun(state: State, action: Action): State {
        return reducers.fold(state, { nextState, next -> next(nextState, action) })
    }
}

class Store<State> {
    private var currentReducer: Reducer<State>
    private var currentState: State
    var innerDispatch: Dispatcher
    private val lock: Object = Object()

    constructor(reducer: Reducer<State>, currentState: State) {
        this.currentReducer = reducer
        this.currentState = currentState
        this.innerDispatch = fun (action: Action): Action {
            synchronized(lock) {
                this.currentState = this.currentReducer(this.currentState, action)
            }
            return action
        }
    }

    fun getState(): State {
        return this.currentState
    }

    fun dispatch(action: Action) : Action {
        return this.innerDispatch(action)
    }
}

fun <State> createStore(reducer: Reducer<State>, preloadedState: State): Store<State> {
    val store = Store<State>(reducer, preloadedState)
    store.dispatch(Action(Init()))
    return store
}

fun <State> createStoreWithEnhancer(reducer: Reducer<State>, preloadedState: State, enhancer: StoreCreator<State>): Store<State> {
    return enhancer(reducer, preloadedState)
}

fun <State> applyMiddleware(middlewares: List<Middleware<State>>): StoreCreator<State> {

    return fun( reducer: Reducer<State>, preloadedState) : Store<State> {
            val store = Store<State>(reducer, preloadedState)
            val originalDispatch = store.innerDispatch
            val chain = middlewares.map({ middleware -> middleware(store)})

            /* Create function composition of all middleware. [f, g, h] -> f(g(h())) */
            val start = middleware<State>()(store)(originalDispatch)
            var resolved = start
            chain.forEach{
                resolved = it(resolved)
            }

            store.innerDispatch = fun(action: Action) : Action {
                return resolved(action)
            }

            store.dispatch(Action(Init()))

            return store
    }
}

fun <State> middleware(): Middleware<State> {
    return { _ ->
        { next ->
            { action ->
                next(action)
            }
        }
    }
}

fun <State> loggingMiddleware(): Middleware<State> {
    return { _ ->
        { next ->
            { action ->
                println("Dispatching action of type: ${action.type}")
                next (action)
            }
        }
    }
  }
