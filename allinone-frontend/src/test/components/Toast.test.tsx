import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import Toast from '../../components/Toast'

describe('Toast', () => {
  it('renders nothing when not visible', () => {
    const { container } = render(
      <Toast message="test" type="info" visible={false} onClose={() => {}} />
    )
    expect(container.firstChild).toBeNull()
  })

  it('renders message when visible', () => {
    render(
      <Toast message="Hello World" type="info" visible={true} onClose={() => {}} />
    )
    expect(screen.getByText('Hello World')).toBeInTheDocument()
  })

  it('applies success background for success type', () => {
    render(
      <Toast message="Success!" type="success" visible={true} onClose={() => {}} />
    )
    const div = screen.getByText('Success!').closest('div')
    expect(div?.className).toContain('bg-green-600')
  })

  it('applies error background for error type', () => {
    render(
      <Toast message="Error!" type="error" visible={true} onClose={() => {}} />
    )
    const div = screen.getByText('Error!').closest('div')
    expect(div?.className).toContain('bg-red-600')
  })

  it('applies info background for info type', () => {
    render(
      <Toast message="Info!" type="info" visible={true} onClose={() => {}} />
    )
    const div = screen.getByText('Info!').closest('div')
    expect(div?.className).toContain('bg-blue-600')
  })

  it('calls onClose when close button clicked', () => {
    const onClose = vi.fn()
    render(
      <Toast message="Dismiss me" type="info" visible={true} onClose={onClose} />
    )
    fireEvent.click(screen.getByText('\u00D7'))
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('auto-dismisses after 4 seconds', () => {
    vi.useFakeTimers()
    const onClose = vi.fn()
    render(
      <Toast message="Auto dismiss" type="info" visible={true} onClose={onClose} />
    )
    expect(onClose).not.toHaveBeenCalled()
    vi.advanceTimersByTime(4000)
    expect(onClose).toHaveBeenCalledTimes(1)
    vi.useRealTimers()
  })
})
