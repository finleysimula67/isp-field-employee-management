type NavigateFn = (path: string) => void
let _navigate: NavigateFn | null = null

export function setNavigate(fn: NavigateFn) {
  _navigate = fn
}

export function appNavigate(path: string) {
  if (_navigate) {
    _navigate(path)
  } else {
    window.location.href = path
  }
}
