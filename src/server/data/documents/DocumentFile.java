package server.data.documents;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class DocumentFile {

	private final File file;
	private final DocumentType type;

	protected DocumentFile(File file) {
		this.file = file;
		type = DocumentType.getType(file.getName());
	}

	public File getFile() {
		return file;
	}

	public DocumentType getType() {
		return type;
	}

	public String getName() {
		return file.getName().substring(0, file.getName().lastIndexOf('.'));
	}

	public enum DocumentType {
		PROPERTIES(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE, ".properties"),
		JSON(SyntaxConstants.SYNTAX_STYLE_JSON, ".json"),
		XML(SyntaxConstants.SYNTAX_STYLE_XML, ".cfg", ".xml"),
		YML(SyntaxConstants.SYNTAX_STYLE_YAML, ".yml", ".yaml"),
		MD(SyntaxConstants.SYNTAX_STYLE_MARKDOWN, ".md"),
		NONE(SyntaxConstants.SYNTAX_STYLE_NONE, ".txt", "");

		public final String style;
		public final String[] endings;

		DocumentType(String style, String... endings) {
			this.style = style;
			this.endings = endings;
		}

		public static DocumentType getType(String fileName) {
			for(DocumentType type : values()) {
				for(String ending : type.endings) {
					if(fileName.endsWith(ending)) return type;
				}
			}
			return NONE;
		}

		public static boolean isDocument(File file) {
			return getType(file.getName()) != NONE;
		}

		public static List<DocumentFile> getDocumentsOfTypeRecursive(File folder, DocumentType type) {
			List<DocumentFile> documents = new ArrayList<>();
			for(DocumentFile document : getAllDocumentsRecursive(folder)) {
				if(document.getType() == type) documents.add(document);
			}
			return documents;
		}

		public static List<DocumentFile> getAllDocumentsRecursive(File folder) {
			List<DocumentFile> documents = new ArrayList<>();
			if(folder == null || !folder.exists() || folder.listFiles() == null) return documents;
			for(File file : folder.listFiles()) {
				if(file.isDirectory()) documents.addAll(getAllDocumentsRecursive(file));
				else if(isDocument(file)) documents.add(new DocumentFile(file) {
					@Override
					public String toString() {
						return file.getName();
					}
				});
			}
			return documents;
		}
	}
}
