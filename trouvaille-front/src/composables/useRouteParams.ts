import { LocationQueryRaw } from 'vue-router'
import router from '../router'
import { Ref } from 'vue'
import { useRoute } from 'vue-router'
import { IRouterParam } from '@/types/routeParams'

export function useRouteParams() {
  const route = useRoute()

  const asString = (t: number | undefined) => {
    if (t === null || t === undefined) {
      return undefined
    }
    return t.toString()
  }

  const applyRouterParams = async (
    routeName: string,
    routerParams: IRouterParam[],
    updating: Ref<boolean>,
  ) => {
    updating.value = true
    const query: LocationQueryRaw = {}
    routerParams.forEach((routerParam) => {
      const value = routerParam.getRouteValue()
      if (value !== undefined && value.length > 0) {
        query[routerParam.paramName] = value
      }
    })
    await router.push({ name: routeName, query })
    updating.value = false
  }

  const initFromRouterParams = (routerParams: IRouterParam[]) => {
    routerParams.forEach((routerParam) => {
      const value = route.query[routerParam.paramName]
      if (value !== null && value !== undefined) {
        routerParam.applyFromRoute(value.toString())
      }
    })
  }

  return {
    asString,
    applyRouterParams,
    initFromRouterParams,
  }
}

export default useRouteParams
