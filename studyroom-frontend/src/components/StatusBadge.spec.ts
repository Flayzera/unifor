import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusBadge from './StatusBadge.vue'

describe('StatusBadge', () => {
  it('renders confirmed state', () => {
    const wrapper = mount(StatusBadge, {
      props: { status: 'CONFIRMED' },
    })

    expect(wrapper.get('[data-testid="status-badge"]').text()).toBe('confirmed')
    expect(wrapper.classes().join(' ')).toContain('text-green-700')
  })
})
