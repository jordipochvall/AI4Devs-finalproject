import { Component, type CSSProperties, type ErrorInfo, type ReactNode } from 'react'
import { withTranslation, type WithTranslation } from 'react-i18next'

interface Props extends WithTranslation {
  children: ReactNode
}

interface State {
  hasError: boolean
}

/**
 * HU-39 — catches render errors anywhere in the tree below it and shows a recoverable message
 * instead of leaving a blank page. Must be a class component: React only calls
 * {@link getDerivedStateFromError}/{@link componentDidCatch} on class components.
 */
class ErrorBoundaryImpl extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // eslint-disable-next-line no-console
    console.error('Unhandled render error caught by ErrorBoundary', error, info)
  }

  private handleReload = () => {
    this.setState({ hasError: false })
    window.location.assign('/')
  }

  render() {
    if (!this.state.hasError) {
      return this.props.children
    }
    const { t } = this.props
    return (
      <div role="alert" style={errorStyles.container}>
        <h1 style={errorStyles.title}>{t('errorBoundary.title')}</h1>
        <p style={errorStyles.message}>{t('errorBoundary.message')}</p>
        <button type="button" className="btn-primary" onClick={this.handleReload}>
          {t('errorBoundary.reload')}
        </button>
      </div>
    )
  }
}

const errorStyles: Record<string, CSSProperties> = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    padding: '2rem',
    textAlign: 'center',
    gap: '1rem',
  },
  title: { margin: 0 },
  message: { margin: 0, maxWidth: '32rem' },
}

export const ErrorBoundary = withTranslation('shared')(ErrorBoundaryImpl)
