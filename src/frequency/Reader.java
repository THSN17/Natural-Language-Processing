package frequency;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Reader {
	private static final long PARTITION_SIZE = 10000;

	private static ArrayList<String> holdList;

	public static String[] readFile(File file) {

		String[] parts;
		String text = "";
		try (FileReader read = new FileReader(file)) {

			long size = 0;
			long length = file.length();

			parts = new String[(int) (length / PARTITION_SIZE) + 1];
			for (int i = 0; i < parts.length; i++) {
				parts[i] = "";
			}

			while (read.ready()) {
				parts[(int) (size / PARTITION_SIZE)] += (char) read.read();
				size++;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "" };
		}

		for (int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].replaceAll("\r\n\r\n", "\r\n").replaceAll("\r\n\r\n", "\r\n").replaceAll("  ", " ")
					.replaceAll("  ", " ").replaceAll("  ", " ").toLowerCase();
			text += parts[i];
		}

		String[] textOneLine = text.split("\r\n");
		String[][] textNoSpace = new String[textOneLine.length][];

		for (int i = 0; i < textOneLine.length; i++) {
			textNoSpace[i] = textOneLine[i].split(" ");
		}

		// Split hyphenated words
		holdList = new ArrayList<>();
		for (int i = 0; i < textNoSpace.length; i++) {
			for (int j = 0; j < textNoSpace[i].length; j++) {
				String s = textNoSpace[i][j];
				if (s.contains("-") && !(s.startsWith("-") || s.endsWith("-"))) {
					String[] hold = textNoSpace[i][j].split("-");
					if (!(hold[0].equals("") || hold[hold.length - 1].equals(""))) {
						textNoSpace[i][j] = hold[0];
						holdList.add(hold[hold.length - 1]);
					}
				}
			}
		}

		int noSpaceLength = 0;
		for (String[] s : textNoSpace) {
			noSpaceLength += s.length;
		}

		String[] words = new String[noSpaceLength];
		int count = 0;
		for (int i = 0; i < textNoSpace.length && count < noSpaceLength; i++) {
			for (int j = 0; j < textNoSpace[i].length && count < noSpaceLength; j++) {
				words[count] = textNoSpace[i][j];
				count++;
			}
		}

		// Add words split around hyphens from before
		for (int i = noSpaceLength - count + 1, j = 0; j < holdList.size(); i++, j++) {
			words[i] = holdList.get(j);
		}

		return words;
	}

	public static String[] readFile(String file) {
		return readFile(new File(file));
	}

	public static String readTitle(String file) {
		String title = "";
		try (FileReader read = new FileReader(file)) {
			while (read.ready()) {
				char c = (char) read.read();
				if (c == '\r' || c == '\n')
					break;
				title += c;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return title;
	}

	public static String[] readAuthors(String file) {
		String[] authors = new String[] {};
		String authorsUnseparated = "";
		char c;
		boolean lastNewline = false;
		try (FileReader read = new FileReader(file)) {
			while (read.ready()) {
				if ((char) read.read() == '\n')
					break;
			}
			while (read.ready()) {
				c = (char) read.read();
				if (c == '\n') {
					if (lastNewline) {
						break;
					}
					lastNewline = true;
					authorsUnseparated += c;
				} else if (c != '\r') {
					lastNewline = false;
					authorsUnseparated += c;
				}
			}
			
			authors = authorsUnseparated.split("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authors;
	}
}