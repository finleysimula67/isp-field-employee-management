import { render } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import Skeleton from '../../components/Skeleton'

describe('Skeleton', () => {
  it('renders text variant by default', () => {
    const { container } = render(<Skeleton />)
    const innerDiv = container.querySelector('.space-y-2')
    expect(innerDiv).toBeInTheDocument()
  })

  it('renders text variant with correct count', () => {
    const { container } = render(<Skeleton variant="text" count={3} />)
    const skeletons = container.querySelector('.space-y-2')
    expect(skeletons?.children).toHaveLength(3)
  })

  it('renders card variant', () => {
    const { container } = render(<Skeleton variant="card" count={1} />)
    const card = container.querySelector('.bg-white.rounded-xl')
    expect(card).toBeInTheDocument()
  })

  it('renders table-row variant with correct rows', () => {
    const { container } = render(<Skeleton variant="table-row" count={2} />)
    const rows = container.querySelector('.space-y-2')
    expect(rows?.children).toHaveLength(2)
  })
})
