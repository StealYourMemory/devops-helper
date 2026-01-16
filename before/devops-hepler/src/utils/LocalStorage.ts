export default class LocalStorageUtil {
    static set(key: string, value: string): void {
        localStorage.setItem(key, value);
    }

    static get(key: string): string | null {
        return localStorage.getItem(key);
    }

    static remove(key: string): void {
        localStorage.removeItem(key);
    }

    static clear(): void {
        localStorage.clear();
    }
}