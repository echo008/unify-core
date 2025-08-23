class WeChatEnhancedBridge {
    constructor() {
        this.componentInstances = new Map();
        this.stateSubscriptions = new Map();
        this.kotlinRuntime = null;
        this.pageStack = [];
        this.eventQueue = [];
        this.isInitialized = false;
    }
    async initKotlinRuntime() {
        try {
            this.kotlinRuntime = await import('./kotlin-runtime.js');
            this.isInitialized = true;
            this.processEventQueue();
        } catch (error) {
            console.error('Failed to initialize Kotlin runtime:', error);
        }
    }
    onLoad(options) {
        console.log('Page loaded with options:', options);
        this.pageStack.push({
            route: getCurrentPages()[getCurrentPages().length - 1].route,
            options: options,
            timestamp: Date.now()
        });
        if (!this.isInitialized) { this.initKotlinRuntime(); }
        this.emitEvent('page:load', { options });
    }
    onShow() {
        console.log('Page shown');
        this.emitEvent('page:show', {});
        this.componentInstances.forEach((instance) => { if (instance.onShow) instance.onShow(); });
    }
    onHide() {
        console.log('Page hidden');
        this.emitEvent('page:hide', {});
        this.componentInstances.forEach((instance) => { if (instance.onHide) instance.onHide(); });
    }
    onUnload() {
        console.log('Page unloaded');
        this.emitEvent('page:unload', {});
        this.componentInstances.clear();
        this.stateSubscriptions.clear();
        this.pageStack.pop();
    }
    handleComponentEvent(componentId, eventType, eventData) {
        console.log('Component event:', { componentId, eventType, eventData });
        const event = { componentId, type: eventType, data: eventData, timestamp: Date.now() };
        if (!this.isInitialized) { this.eventQueue.push(event); return; }
        try {
            if (this.kotlinRuntime && this.kotlinRuntime.handleEvent) { this.kotlinRuntime.handleEvent(event); }
            this.processLocalEvent(event);
        } catch (error) { console.error('Error handling component event:', error); }
    }
    processLocalEvent(event) {
        const { componentId, type, data } = event;
        switch (type) {
            case 'tap': this.handleTapEvent(componentId, data); break;
            case 'input': this.handleInputEvent(componentId, data); break;
            case 'scroll': this.handleScrollEvent(componentId, data); break;
            case 'load':
            case 'error': this.handleMediaEvent(componentId, type, data); break;
            default: console.log('Unhandled event type:', type);
        }
    }
    handleTapEvent(componentId, data) { this.emitEvent('component:tap', { componentId, data }); }
    handleInputEvent(componentId, data) {
        const value = data.detail ? data.detail.value : data.value;
        this.updateComponentState(componentId, { value });
        this.emitEvent('component:input', { componentId, value });
    }
    handleScrollEvent(componentId, data) {
        const scrollData = { scrollTop: data.detail.scrollTop, scrollLeft: data.detail.scrollLeft };
        this.emitEvent('component:scroll', { componentId, scrollData });
    }
    handleMediaEvent(componentId, type, data) { this.emitEvent(`component:${type}`, { componentId, data }); }
    updateComponentState(componentId, newState) {
        const instance = this.componentInstances.get(componentId);
        if (instance) {
            Object.assign(instance.state, newState);
            if (instance.onStateChange) { instance.onStateChange(newState); }
        }
        const currentPage = getCurrentPages()[getCurrentPages().length - 1];
        if (currentPage && currentPage.updateState) { currentPage.updateState({ [componentId]: newState }); }
    }
    emitEvent(eventName, eventData) {
        const event = { name: eventName, data: eventData, timestamp: Date.now() };
        console.log('Emitting event:', event);
        if (this.kotlinRuntime && this.kotlinRuntime.onEvent) { this.kotlinRuntime.onEvent(event); }
    }
    processEventQueue() {
        while (this.eventQueue.length > 0) {
            const event = this.eventQueue.shift();
            this.handleComponentEvent(event.componentId, event.type, event.data);
        }
    }
    showToast(options) { wx.showToast({ title: options.title || '提示', icon: options.icon || 'none', duration: options.duration || 2000, mask: options.mask || false }); }
    navigateTo(url) { wx.navigateTo({ url }); }
    redirectTo(url) { wx.redirectTo({ url }); }
    navigateBack(delta = 1) { wx.navigateBack({ delta }); }
    async request(options) { return new Promise((resolve, reject) => { wx.request({ ...options, success: resolve, fail: reject }); }); }
    async getStorage(key) { return new Promise((resolve, reject) => { wx.getStorage({ key, success: (res) => resolve(res.data), fail: reject }); }); }
    async setStorage(key, data) { return new Promise((resolve, reject) => { wx.setStorage({ key, data, success: resolve, fail: reject }); }); }
    getCurrentPage() { const pages = getCurrentPages(); return pages[pages.length - 1]; }
    getPageStack() { return [...this.pageStack]; }
}
const bridge = new WeChatEnhancedBridge();
module.exports = {
    onLoad: bridge.onLoad.bind(bridge),
    onShow: bridge.onShow.bind(bridge),
    onHide: bridge.onHide.bind(bridge),
    onUnload: bridge.onUnload.bind(bridge),
    handleComponentEvent: bridge.handleComponentEvent.bind(bridge),
    updateComponentState: bridge.updateComponentState.bind(bridge),
    showToast: bridge.showToast.bind(bridge),
    navigateTo: bridge.navigateTo.bind(bridge),
    request: bridge.request.bind(bridge),
    getStorage: bridge.getStorage.bind(bridge),
    setStorage: bridge.setStorage.bind(bridge)
};
