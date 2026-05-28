// Analytics tracking utility for Hotjar and Mixpanel

export const trackEvent = (eventName: string, properties?: Record<string, any>) => {
  // Track in Mixpanel
  if (typeof window !== 'undefined' && window.mixpanel) {
    window.mixpanel.track(eventName, properties || {});
  }

  // Track in Hotjar (custom event)
  if (typeof window !== 'undefined' && window.hj) {
    window.hj('event', eventName);
  }

  // Console log for debugging
  console.log(`[Analytics] Event: ${eventName}`, properties || {});
};

export const trackSignupClick = () => {
  trackEvent('signup_clicked', {
    timestamp: new Date().toISOString(),
  });
};

export const trackBlogPostViewed = (slug: string) => {
  trackEvent('blog_post_viewed', {
    slug,
    timestamp: new Date().toISOString(),
  });
};

export const trackFeatureViewed = (feature: string) => {
  trackEvent('feature_viewed', {
    feature,
    timestamp: new Date().toISOString(),
  });
};

export const trackEmailSignup = (email: string) => {
  trackEvent('email_signup', {
    email,
    timestamp: new Date().toISOString(),
  });
};

export const trackUseCaseViewed = (useCase: string) => {
  trackEvent('use_case_viewed', {
    useCase,
    timestamp: new Date().toISOString(),
  });
};

export const trackComparisonViewed = () => {
  trackEvent('comparison_page_viewed', {
    timestamp: new Date().toISOString(),
  });
};

// Add Mixpanel type to window
declare global {
  interface Window {
    mixpanel?: any;
    hj?: any;
  }
}
