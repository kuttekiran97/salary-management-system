import { useCallback, useEffect, useState } from 'react'

export interface ApiState<T> {
  data: T | undefined
  error: Error | undefined
  loading: boolean
  reload: () => void
}

/**
 * Loads data when the component mounts and whenever `deps` change.
 * Responses that arrive after a newer request has started are ignored,
 * so fast typing in a search box cannot show stale results.
 */
export function useApi<T>(load: () => Promise<T>, deps: unknown[]): ApiState<T> {
  const [data, setData] = useState<T>()
  const [error, setError] = useState<Error>()
  const [loading, setLoading] = useState(true)
  const [version, setVersion] = useState(0)

  // eslint-disable-next-line react-hooks/exhaustive-deps -- callers list the inputs of `load` in `deps`
  const stableLoad = useCallback(load, deps)

  useEffect(() => {
    let current = true
    setLoading(true)
    stableLoad()
      .then((result) => {
        if (current) {
          setData(result)
          setError(undefined)
        }
      })
      .catch((e: Error) => current && setError(e))
      .finally(() => current && setLoading(false))
    return () => {
      current = false
    }
  }, [stableLoad, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])
  return { data, error, loading, reload }
}
