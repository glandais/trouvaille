export interface ApplyFromRoute {
  (input: string): void
}

export interface GetRouteValue {
  (): string | undefined
}

export interface IRouterParam {
  paramName: string
  applyFromRoute: ApplyFromRoute
  getRouteValue: GetRouteValue
}

export class RouterParam<T> implements IRouterParam {
  paramName: string
  defaultValue: T
  applyFromRoute: ApplyFromRoute
  getRouteValue: GetRouteValue
  constructor(
    paramName: string,
    defaultValue: T,
    applyFromRoute: ApplyFromRoute,
    getRouteValue: GetRouteValue,
  ) {
    this.paramName = paramName
    this.defaultValue = defaultValue
    this.applyFromRoute = applyFromRoute
    this.getRouteValue = getRouteValue
  }
}
