export function encodeToBase64(input: string): string {
    // 将字符串转换为UTF-8编码的字节数组
    const encoder = new TextEncoder();
    const utf8 = encoder.encode(input);

    // 将字节数组转换为Base64字符串
    let binary = '';
    const len = utf8.byteLength;
    for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(utf8[i]);
    }
    return btoa(binary);
}
export function decodeFromBase64(encoded: string): string {
    // 将Base64编码的字符串解码为二进制字符串
    const binary = atob(encoded);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }

    // 将字节数组解码为UTF-8字符串
    const decoder = new TextDecoder('utf-8');
    return decoder.decode(bytes);
}