package org.automation.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import org.automation.records.FileNode;

@WebServlet("/FileListServlet")
public class FileListServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        String folderPath = request.getParameter("folderPath");
        JsonObject jsonResponse = new JsonObject();

        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Folder not found: " + folderPath);
            } else {
                List<FileNode> nodes = scanDirectory(path);
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", new Gson().toJsonTree(nodes));
                jsonResponse.addProperty("fileCount", countJsonFiles(nodes));
            }
        } catch (Exception e) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
        }

        response.getWriter().write(jsonResponse.toString());
    }

    private List<FileNode> scanDirectory(Path path) throws IOException {
        List<FileNode> nodes = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                FileNode node = new FileNode();
                node.setName(entry.getFileName().toString());
                node.setPath(entry.toString());
                node.setFolder(Files.isDirectory(entry));

                if (Files.isDirectory(entry)) {
                    node.setChildren(scanDirectory(entry));
                } else if (entry.toString().endsWith(".json")) {
                    node.setChildren(new ArrayList<>());
                }

                if (Files.isDirectory(entry) || entry.toString().endsWith(".json")) {
                    nodes.add(node);
                }
            }
        }
        nodes.sort(Comparator.comparing(FileNode::isFolder).reversed()
                .thenComparing(FileNode::getName));
        return nodes;
    }

    private int countJsonFiles(List<FileNode> nodes) {
        int count = 0;
        for (FileNode node : nodes) {
            if (!node.isFolder()) count++;
            if (node.getChildren() != null) {
                count += countJsonFiles(node.getChildren());
            }
        }
        return count;
    }
}
