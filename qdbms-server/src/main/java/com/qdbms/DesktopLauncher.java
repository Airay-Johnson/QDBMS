package com.qdbms;

import org.springframework.boot.SpringApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

/**
 * 桌面启动器 — 自动打开浏览器 + 系统托盘
 * 打包 EXE 时使用此类作为主类
 */
public class DesktopLauncher {

    private static final String APP_NAME = "QDBMS 问卷系统";
    private static final String URL = "http://localhost:8080";

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 启动 Spring Boot
        var context = SpringApplication.run(QdbmsApplication.class, args);
        var port = context.getEnvironment().getProperty("server.port", "8080");

        // 自动打开浏览器
        SwingUtilities.invokeLater(() -> {
            openBrowser("http://localhost:" + port);
            setupTray(port, () -> {
                SpringApplication.exit(context, () -> 0);
                System.exit(0);
            });
        });
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // 等 Spring Boot 完全启动后再开浏览器
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e) {
                        System.out.println("浏览器打开失败: " + e.getMessage());
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println("系统不支持自动打开浏览器: " + e.getMessage());
        }
    }

    private static void setupTray(String port, Runnable onExit) {
        if (!SystemTray.isSupported()) {
            System.out.println("系统托盘不支持");
            return;
        }

        try {
            var tray = SystemTray.getSystemTray();
            var icon = createTrayIcon();
            var popup = new PopupMenu();

            var openItem = new MenuItem("打开 QDBMS");
            openItem.addActionListener(e -> openBrowser("http://localhost:" + port));
            popup.add(openItem);

            popup.addSeparator();

            var exitItem = new MenuItem("退出");
            exitItem.addActionListener(e -> {
                tray.remove(icon);
                onExit.run();
            });
            popup.add(exitItem);

            icon.setPopupMenu(popup);
            icon.setToolTip(APP_NAME + " - http://localhost:" + port);
            tray.add(icon);

            // 双击托盘图标打开
            icon.addActionListener(e -> openBrowser("http://localhost:" + port));

        } catch (Exception e) {
            System.out.println("托盘初始化失败: " + e.getMessage());
        }
    }

    private static TrayIcon createTrayIcon() {
        // 用代码绘制一个 16x16 的图标
        int w = 16, h = 16;
        var img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        // 蓝色背景
        g.setColor(new Color(64, 158, 255));
        g.fillRect(0, 0, w, h);
        // 白色文字 Q
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String letter = "Q";
        int x = (w - fm.stringWidth(letter)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(letter, x, y);
        g.dispose();

        return new TrayIcon(img, APP_NAME);
    }
}
