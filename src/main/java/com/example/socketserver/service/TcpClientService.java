package com.example.socketserver.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TcpClientService {

    public Map<String, Object> sendMessage(String host, int port, String message) {
        Map<String, Object> result = new HashMap<>();
        Socket socket = null;
        OutputStream out = null;
        InputStream in = null;

        try {
            // 소켓 연결
            socket = new Socket(host, port);
            socket.setSoTimeout(10000); // 10초 타임아웃 설정

            // 출력 스트림 생성
            out = socket.getOutputStream();
            
            // 입력 스트림 생성
            in = socket.getInputStream();

            // 메시지를 바이트 배열로 변환하여 전송
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            out.write(messageBytes);
            out.flush();

            // 응답 수신 (바이너리 데이터도 처리 가능)
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
            
            // 소켓이 닫히거나 타임아웃될 때까지 읽기
            int bytesRead;
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5초 대기
            
            while (System.currentTimeMillis() - startTime < timeout) {
                if (in.available() > 0) {
                    bytesRead = in.read(buffer);
                    if (bytesRead > 0) {
                        responseBytes.write(buffer, 0, bytesRead);
                        startTime = System.currentTimeMillis(); // 데이터를 받으면 타이머 리셋
                    } else {
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(100); // 데이터가 없으면 잠시 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            byte[] responseData = responseBytes.toByteArray();
            String responseHex = bytesToHex(responseData);
            String responseText = new String(responseData, StandardCharsets.UTF_8);

            result.put("success", true);
            result.put("sentMessage", message);
            result.put("responseHex", responseHex);
            result.put("responseText", responseText);
            result.put("responseLength", responseData.length);
            result.put("host", host);
            result.put("port", port);

        } catch (java.net.SocketTimeoutException e) {
            result.put("success", false);
            result.put("error", "연결 타임아웃: " + e.getMessage());
            result.put("sentMessage", message);
        } catch (java.net.ConnectException e) {
            result.put("success", false);
            result.put("error", "연결 실패: " + e.getMessage());
            result.put("sentMessage", message);
        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "IO 오류: " + e.getMessage());
            result.put("sentMessage", message);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "오류 발생: " + e.getMessage());
            result.put("sentMessage", message);
        } finally {
            // 리소스 정리
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // 무시
            }
        }

        return result;
    }

    // 바이트 배열을 16진수 문자열로 변환
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}

