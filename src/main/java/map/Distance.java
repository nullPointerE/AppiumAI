package map;

public class Distance {
    public static int[][] graph;
    public static int INF;

    public static void floydWarshall(int dist[][]) {
        int v = dist.length;
        graph = new int[v][v];
        int i, j, k;
        for (i = 0; i < v; i++)
            for (j = 0; j < v; j++)
                graph[i][j] = dist[i][j];

        for (k = 0; k < v; k++) {
            for (i = 0; i < v; i++) {
                for (j = 0; j < v; j++) {
                    if (graph[i][k] + graph[k][j] < graph[i][j])
                        graph[i][j] = graph[i][k] + graph[k][j];
                }
            }
        }
    }
}
