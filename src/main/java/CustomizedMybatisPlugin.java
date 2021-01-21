import com.google.common.base.Strings;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaReservedWords;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.VisitableElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * add lombok
 * ignore getter setter
 * ignore java reserved words
 * ignore xml sql except selectAll
 */
public class CustomizedMybatisPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addImportedType("lombok.Data");

        topLevelClass.addAnnotation("@Data");
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if (field != null) {
            String name = field.getName();
            if (JavaReservedWords.containsWord(name)) {
                String columnName = introspectedColumn.getActualColumnName();
                String tableName = introspectedTable.getFullyQualifiedTable().toString();
                System.err.println("JavaReservedWord is detected and ignored. table.column = " + tableName + "." + columnName);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapResultMapWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (element != null && element.getElements() != null && !element.getElements().isEmpty()) {
            int elementIndex = 0;
            VisitableElement visitableElement = element.getElements().get(elementIndex);
            if (visitableElement instanceof TextElement) {
                TextElement textElement = (TextElement) visitableElement;
                String content = textElement.getContent();
                //select id, classification, ori_classes, update_date, create_time, is_deleted
                String startStr = "select ";
                if (!Strings.isNullOrEmpty(content)
                        && content.startsWith(startStr)
                        && content.length() > startStr.length()) {
                    List<VisitableElement> oldElementList = element.getElements();
                    List<VisitableElement> newElementList = transform(oldElementList);
                    oldElementList.clear();
                    oldElementList.addAll(newElementList);
                }
            }
        }
        return super.sqlMapSelectAllElementGenerated(element, introspectedTable);
    }

    private List<VisitableElement> transform(List<VisitableElement> srcList) {
        List<String> tokens = new ArrayList<>();
        String selectStr = "select ";
        String fromStr = "from ";
        int index = 0;
        for (; index < srcList.size(); index++) {
            VisitableElement element = srcList.get(index);
            if (element instanceof TextElement) {
                TextElement textElement = (TextElement) element;
                String content = textElement.getContent();
                if (content.startsWith(fromStr)) {
                    break;
                }
                boolean hasSelect = content.startsWith(selectStr);
                String[] rawTokens = content.split(" ");
                for (int i = 0; i < rawTokens.length; i++) {
                    if (hasSelect && i == 0) {
                        tokens.add(rawTokens[i]);
                    } else {
                        tokens.add("    " + rawTokens[i]);
                    }
                }
            } else {
                break;
            }
        }

        List<VisitableElement> rtn = new ArrayList<>();
        for (String token : tokens) {
            rtn.add(new TextElement(token));
        }
        rtn.addAll(srcList.subList(index, srcList.size()));
        return rtn;
    }
}
