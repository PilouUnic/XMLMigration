package fr.xml.migration;

import java.io.Serializable;
import java.util.List;

/**
 * Classe POJO materialisant une ligne de fichier CSV ou XLS extraite de ce dernier.
 * @author REACTIS
 * @since 01.00
 */
public class RowElement implements Serializable {

    /**
     * Serial UID par defaut.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Variable contenant la liste des types de l'objet RowElement.
     * @author REACTIS
     * @since 01.00
     */
    public static enum RowType {

        /** Variable indiquant que le type est HEADER */
        HEAEDER,

        /** Variable indiquant que le type est CONTENT */
        CONTENT;
    };

    /** Variable contenant le type de l'objet RowElement. */
    private RowType rowType;

    /** Variable contenant les lignes d'un fichier CSV ou Excel. */
    private List<String> contentElements;

    /**
     * Constructeur par defaut.
     */
    public RowElement() {
        super();
    }

    /**
     * Constructeur avec la totalite des propriete de l'objet.
     * @param rowType Type de l'objet (HEADER ou CONTENT)
     * @param contentElements Liste de string representant les elements d'une ligne CSV.
     */
    public RowElement(final RowType rowType, final List<String> contentElements) {
        this.rowType = rowType;
        this.contentElements = contentElements;
    }

    /**
     * Accesseur de la propriete contentElements.
     * @return the contentElements.
     */
    public List<String> getContentElements() {
        return contentElements;
    }

    /**
     * Accesseur de la propriete rowType.
     * @param contentElements the contentElements to set.
     */
    public void setContentElements(final List<String> contentElements) {
        this.contentElements = contentElements;
    }

    /**
     * Accesseur de la propriete rowType.
     * @return the rowType
     */
    public RowType getRowType() {
        return rowType;
    }

    /**
     * Accesseur de la propriete rowType.
     * @param rowType the rowType to set
     */
    public void setRowType(final RowType rowType) {
        this.rowType = rowType;
    }

}
