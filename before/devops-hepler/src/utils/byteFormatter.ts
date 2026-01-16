export  default function formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const sizes: string[] = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const k: number = 1024;

    const i: number = Math.floor(Math.log(bytes) / Math.log(k));

    if (i === 0) return `${bytes} ${sizes[i]}`; // Return in Bytes if less than 1 KB

    return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
}
