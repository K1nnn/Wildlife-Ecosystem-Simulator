package com.ecosystem.sim.util;

import com.badlogic.gdx.math.Vector2;
import com.ecosystem.sim.map.MapManager;
import java.util.*;

/**
 * Công cụ tìm đường sử dụng BFS (Breadth-First Search)
 * Được sử dụng để tìm nước, thức ăn, hoặc lối thoát
 */
public class PathFinding {
    
    private static final int TILE_SIZE = 16; // Kích thước tile
    

    /**
     * Tìm vị trí trống gần nhất
     * Dùng để dùng động vật nhường đường cho nhau
     */
    public static Vector2 findNearbyEmptySpot(Vector2 currentPos, MapManager mapManager, 
                                               float searchRadius) {
        for (int i = 0; i < 8; i++) {
            float angle = (i / 8f) * 360;
            Vector2 offset = new Vector2(searchRadius, 0).rotateRad((float) Math.toRadians(angle));
            Vector2 testPos = new Vector2(currentPos).add(offset);
            
            if (!mapManager.isObstacle(testPos.x, testPos.y)) {
                return testPos;
            }
        }
        
        return currentPos; // Không tìm thấy, ở lại chỗ cũ
    }

    /**
     * Lớp Node hỗ trợ thuật toán tìm đường A*
     */
    private static class AStarNode implements Comparable<AStarNode> {
        int x, y;
        float g; // Chi phí từ điểm bắt đầu
        float h; // Chi phí ước lượng tới điểm đích (Heuristic)
        float f; // Tổng chi phí (g + h)
        AStarNode parent;

        AStarNode(int x, int y, float g, float h, AStarNode parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }

        @Override
        public int compareTo(AStarNode o) {
            return Float.compare(this.f, o.f);
        }
    }

    /**
     * Tìm đường tối ưu từ điểm bắt đầu tới điểm đích bằng thuật toán A* (A-Star)
     * Tránh đi xuyên qua góc tường hoặc chui vào bụi cây vẽ sẵn trong file .tmx
     */
    public static List<Vector2> findAStarPath(Vector2 startPos, Vector2 targetPos, MapManager mapManager) {
        return findAStarPath(startPos, targetPos, mapManager, false);
    }

    /**
     * Tìm đường tối ưu từ điểm bắt đầu tới điểm đích bằng thuật toán A* (A-Star), hỗ trợ tùy chọn lội nước
     */
    public static List<Vector2> findAStarPath(Vector2 startPos, Vector2 targetPos, MapManager mapManager, boolean ignoreWater) {
        int startTileX = (int) (startPos.x / TILE_SIZE);
        int startTileY = (int) (startPos.y / TILE_SIZE);
        int targetTileX = (int) (targetPos.x / TILE_SIZE);
        int targetTileY = (int) (targetPos.y / TILE_SIZE);

        // Giới hạn trong kích thước bản đồ 50x50
        startTileX = Math.max(0, Math.min(49, startTileX));
        startTileY = Math.max(0, Math.min(49, startTileY));
        targetTileX = Math.max(0, Math.min(49, targetTileX));
        targetTileY = Math.max(0, Math.min(49, targetTileY));

        List<Vector2> path = new ArrayList<>();

        if (startTileX == targetTileX && startTileY == targetTileY) {
            path.add(new Vector2(targetPos));
            return path;
        }

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[50][50];
        float[][] gScores = new float[50][50];
        for (int i = 0; i < 50; i++) {
            Arrays.fill(gScores[i], Float.MAX_VALUE);
        }

        // Sử dụng Manhattan Distance làm heuristic
        float startH = Math.abs(startTileX - targetTileX) + Math.abs(startTileY - targetTileY);
        AStarNode startNode = new AStarNode(startTileX, startTileY, 0, startH, null);
        openSet.add(startNode);
        gScores[startTileX][startTileY] = 0;

        AStarNode targetNode = null;
        int maxIterations = 1000; // Giới hạn số lượt quét để tránh đơ game
        int iterations = 0;

        // 8 hướng di chuyển (4 hướng thẳng, 4 hướng chéo)
        int[][] directions = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0},     // Cardinal
            {1, 1}, {-1, 1}, {1, -1}, {-1, -1}    // Diagonal
        };

        while (!openSet.isEmpty() && iterations++ < maxIterations) {
            AStarNode current = openSet.poll();

            if (closedSet[current.x][current.y]) continue;
            closedSet[current.x][current.y] = true;

            if (current.x == targetTileX && current.y == targetTileY) {
                targetNode = current;
                break;
            }

            for (int i = 0; i < directions.length; i++) {
                int nx = current.x + directions[i][0];
                int ny = current.y + directions[i][1];

                if (nx < 0 || nx >= 50 || ny < 0 || ny >= 50) continue;
                
                boolean isObstacle = ignoreWater ? mapManager.isTileObstacleIgnoreWater(nx, ny) : mapManager.isTileObstacle(nx, ny);
                if (isObstacle) continue;

                // NGĂN CHẶN CẮT GÓC (Corner Cutting):
                // Nếu đi chéo, góc kề kề 2 bên của hướng đi không được là vật cản
                if (i >= 4) {
                    int cx1 = current.x + directions[i][0];
                    int cy1 = current.y;
                    int cx2 = current.x;
                    int cy2 = current.y + directions[i][1];
                    
                    boolean obs1 = ignoreWater ? mapManager.isTileObstacleIgnoreWater(cx1, cy1) : mapManager.isTileObstacle(cx1, cy1);
                    boolean obs2 = ignoreWater ? mapManager.isTileObstacleIgnoreWater(cx2, cy2) : mapManager.isTileObstacle(cx2, cy2);
                    if (obs1 || obs2) {
                        continue; // Bỏ qua nếu có vật cản sát góc để tránh kẹt
                    }
                }

                float weight = (i < 4) ? 1.0f : 1.414f;
                float tentativeG = current.g + weight;

                if (tentativeG < gScores[nx][ny]) {
                    gScores[nx][ny] = tentativeG;
                    float h = Math.abs(nx - targetTileX) + Math.abs(ny - targetTileY);
                    AStarNode neighborNode = new AStarNode(nx, ny, tentativeG, h, current);
                    openSet.add(neighborNode);
                }
            }
        }

        if (targetNode != null) {
            AStarNode curr = targetNode;
            while (curr != null) {
                // Thêm tọa độ thế giới (tâm của ô tile 16x16)
                path.add(0, new Vector2(curr.x * TILE_SIZE + TILE_SIZE / 2f, curr.y * TILE_SIZE + TILE_SIZE / 2f));
                curr = curr.parent;
            }

            // Thay thế điểm cuối cùng của path bằng tọa độ đích chính xác
            if (!path.isEmpty()) {
                path.set(path.size() - 1, new Vector2(targetPos));
            }
        }
        return path;
    }
}
