export default class SessionStorageUtil {
    static set(key: string, value: string): void {
        sessionStorage.setItem(key, value);
    }

    static get(key: string): string | null {
        return sessionStorage.getItem(key);
    }

    static remove(key: string): void {
        sessionStorage.removeItem(key);
    }

    static clear(): void {
        sessionStorage.clear();
    }
}