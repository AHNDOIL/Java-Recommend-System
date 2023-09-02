import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;


class TextFileReader {

    private final String filePath;

    public TextFileReader(String filePath) {
        this.filePath = filePath;
    }

    public ArrayList<TreeMap<String, Double>> read() {
        BufferedReader reader = null;
        StringTokenizer st;

        try {

            reader = new BufferedReader(new FileReader(filePath));

            String line;
            int size;
            int user;
            String content;
            Double score;


            st = new StringTokenizer(reader.readLine());
            size = Integer.parseInt(st.nextToken());

            CompAlpabet compAlpabet = new CompAlpabet();
            Comparator<String> compAlpabetThenNumber = compAlpabet.thenComparing(new CompThenByNumber());

            ArrayList<TreeMap<String, Double>> matrix = new ArrayList<TreeMap<String, Double>>(size);
            for (int i = 0; i < size; i++) {
                matrix.add(i, new TreeMap<String, Double>(compAlpabetThenNumber));
            }

            while ((line = reader.readLine()) != null) {
                st = new StringTokenizer(line);

                while (st.hasMoreTokens()) {

                    user = Integer.parseInt(st.nextToken());
                    content = st.nextToken();
                    score = Double.parseDouble(st.nextToken());

                    matrix.get(user).put(content, score);
                }

            }
            return matrix;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

class NormalizationScore {
    private final ArrayList<TreeMap<String, Double>> matrix;

    public NormalizationScore(ArrayList<TreeMap<String, Double>> matrix) {
        this.matrix = matrix;
    }

    public void run() {
        for (int i = 0; i < matrix.size(); i++) {

            Double total = 0.0;

            for (Double a : matrix.get(i).values()) {
                total += a;
            }

            total /= matrix.get(i).size();

            for (String key : matrix.get(i).keySet()) {
                matrix.get(i).put(key, matrix.get(i).get(key) - total);
            }

        }
    }
}

class ComputingSimilarity {
    private final ArrayList<TreeMap<String, Double>> matrix;
    private final PriorityQueue<UserToSimilarity> similarityQueue;
    private final int target;

    public ComputingSimilarity(ArrayList<TreeMap<String, Double>> matrix, int target, PriorityQueue<UserToSimilarity> similarityQueue) {
        this.matrix = matrix;
        this.target = target;
        this.similarityQueue = similarityQueue;
    }

    public void run() {


        Double bottom2 = 0.0;
        for (String key : matrix.get(target).keySet()) {
            bottom2 += Math.pow(matrix.get(target).get(key), 2);
        }


        for (int i = 0; i < matrix.size(); i++) {

            if (i == target) continue;

            Double top = 0.0;
            Double bottom1 = 0.0;


            for (String key : matrix.get(i).keySet()) {

                bottom1 += Math.pow(matrix.get(i).get(key), 2);

                if (matrix.get(target).get(key) != null) {
                    top += matrix.get(i).get(key) * matrix.get(target).get(key);

                }
            }

            Double bottom = Math.sqrt(bottom1) * Math.sqrt(bottom2);

            if (bottom == 0) continue;

            Double result = top / bottom;

            similarityQueue.add(new UserToSimilarity(i, result));

        }

    }
}

class RecommendingContents {

    private final ArrayList<UserToSimilarity> userToSimilarityList;
    private final TreeMap<String, Double> resultMap;
    private final ArrayList<TreeMap<String, Double>> matrix;
    private final int target;
    private final int reference;

    public RecommendingContents(ArrayList<UserToSimilarity> userToSimilarityList, TreeMap<String, Double> resultMap, ArrayList<TreeMap<String, Double>> matrix, int target, int reference) {
        this.userToSimilarityList = userToSimilarityList;
        this.resultMap = resultMap;
        this.matrix = matrix;
        this.target = target;
        this.reference = reference;
    }

    public void run() {

        Set<String> targetContents = new HashSet<String>();

        for (String content : matrix.get(target).keySet()) {
            targetContents.add(content);
        }

        for (int i = 0; i < reference; i++) {

            Set<String> referenceContents = new HashSet<String>();

            int referenceUser = userToSimilarityList.get(i).getUser();
            Double referenceSimilarity = userToSimilarityList.get(i).getSimilarity();


            for (String content : matrix.get(referenceUser).keySet()) {
                referenceContents.add(content);
            }

            referenceContents.removeAll(targetContents);

            for (String content : referenceContents) {

                Double sum = referenceSimilarity * matrix.get(referenceUser).get(content);

                if (resultMap.containsKey(content)) resultMap.put(content, resultMap.get(content) + sum);
                else resultMap.put(content, sum);

            }
        }
    }

}

class CompAlpabet implements Comparator<String> {
    public int compare(String str1, String str2) {

        char i = str1.charAt(0);
        char j = str2.charAt(0);

        return (i < j) ? -1 : (i > j) ? 1 : 0;

    }
}

class CompThenByNumber implements Comparator<String> {
    public int compare(String str1, String str2) {
        int i = Integer.parseInt(str1.substring(1));
        int j = Integer.parseInt(str2.substring(1));
        return (i < j) ? -1 : (i > j) ? 1 : 0;
    }
}

class UserToSimilarity implements Comparable<UserToSimilarity> {
    private final int user;
    private final Double similarity;

    public UserToSimilarity(int user, Double similarity) {
        this.user = user;
        this.similarity = similarity;
    }

    public int compareTo(UserToSimilarity o) {
        int result = Double.compare(o.similarity, similarity);
        if (result == 0) {
            result = Integer.compare(user, o.user);
        }
        return result;
    }

    public int getUser() {
        return user;
    }

    public Double getSimilarity() {
        return similarity;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.000000");

        return "사용자 id: " + +user + ", 유사도: " + df.format(similarity);
    }
}


class MyTreeMap extends TreeMap<String, Double> {

    public MyTreeMap(Map<String, Double> map, Comparator<String> comparator) {
        super(comparator);
        putAll(map);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.000");
        sb.append("[");
        for (String key : keySet()) {
            sb.append("(").append(key).append(", ").append(df.format(get(key))).append(")").append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }
}

public class HW1 {
    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        String fileName = st.nextToken();
        int target = Integer.parseInt(st.nextToken());
        int reference = Integer.parseInt(st.nextToken());
        int count = Integer.parseInt(st.nextToken());

        TextFileReader reader = new TextFileReader(fileName);

        ArrayList<TreeMap<String, Double>> matrix = reader.read();

        NormalizationScore normalizationScore = new NormalizationScore(matrix);
        normalizationScore.run();


        PriorityQueue<UserToSimilarity> similarityQueue = new PriorityQueue<>();

        ComputingSimilarity similarityScore = new ComputingSimilarity(matrix, target, similarityQueue);
        similarityScore.run();

        CompAlpabet compAlpabet = new CompAlpabet();
        Comparator<String> compAlpabetThenNumber = compAlpabet.thenComparing(new CompThenByNumber());

        System.out.println("1. 사용자 " + target + "의 콘텐츠와 정규화 점수: ");
        MyTreeMap treeMap = new MyTreeMap(matrix.get(target), compAlpabetThenNumber);
        System.out.println("\t" + treeMap);
        System.out.println();


        ArrayList<UserToSimilarity> userToSimilarityList = new ArrayList<>(reference);

        System.out.println("2. 유사한 사용자 id와 유사도 리스트");
        for (int i = 0; i < reference; i++) {
            userToSimilarityList.add(similarityQueue.peek());
            System.out.println("\t" + similarityQueue.poll());
        }
        System.out.println();

        TreeMap<String, Double> resultMap = new TreeMap<>();

        RecommendingContents recommendingContents = new RecommendingContents(userToSimilarityList, resultMap, matrix, target, reference);
        recommendingContents.run();

        Comparator<String> compThenByNumber = new CompThenByNumber();
        Comparator<String> compValue = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int result = resultMap.get(o2).compareTo(resultMap.get(o1));
                if (result == 0) {
                    result = compAlpabetThenNumber.compare(o1, o2);
                }
                return result;
            }
        };


        MyTreeMap treeMap2 = new MyTreeMap(resultMap, compValue);

        System.out.println("3. 사용자 " + target + "에게 추천할 콘텐츠와 추천 점수");

        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.000");
        int clock = 0;
        sb.append("[");
        for (String key : treeMap2.keySet()) {
            clock++;
            sb.append("(").append(key).append(", ").append(df.format(treeMap2.get(key))).append(")").append(", ");
            if (clock == count) break;
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");

        System.out.println("\t" + sb);

    }

}
