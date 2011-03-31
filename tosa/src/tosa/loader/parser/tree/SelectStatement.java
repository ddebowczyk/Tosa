package tosa.loader.parser.tree;

import tosa.loader.SQLParameterInfo;
import tosa.loader.data.DBData;
import tosa.loader.parser.Token;

import java.util.*;

public class SelectStatement extends SQLParsedElement implements IRootParseElement {

  private SQLParsedElement _quantifier;
  private SQLParsedElement _selectList;
  private TableExpression _tableExpr;
  private List<SQLParameterInfo> _parameters;
  private List<VariableExpression> _variables;

  public SelectStatement(Token start, Token end, SQLParsedElement quantifier, SQLParsedElement selectList, TableExpression tableExpr) {
    super(start, end, quantifier, selectList, tableExpr);
    _quantifier = quantifier;
    _selectList = selectList;
    _tableExpr = tableExpr;
  }

  @Override
  protected void toSQL(boolean prettyPrint, int indent, StringBuilder sb, Map<String, Object> values) {
    pp(prettyPrint, indent, "SELECT ", sb);
    if (_quantifier != null) {
      _quantifier.toSQL(prettyPrint, indent, sb, values);
    }
    _selectList.toSQL(prettyPrint, indent, sb, values);
    sb.append("\n");
    _tableExpr.toSQL(prettyPrint, indent, sb, values);
  }

  public TableExpression getTableExpression() {
    return _tableExpr;
  }

  public SQLParsedElement getSelectList() {
    return _selectList;
  }

  public void verify(DBData dbData) {
    super.verify(dbData);
    _variables = determineVariables(dbData);
    _parameters = determineParameters(dbData);
  }

  private List<SQLParameterInfo> determineParameters(DBData dbData) {
    Map<String, SQLParameterInfo> pis = new LinkedHashMap<String, SQLParameterInfo>();
    for (VariableExpression var : _variables) {
      SQLParameterInfo pi = pis.get(var.getName());
      if (pi == null) {
        pi = new SQLParameterInfo(var.getName(), null);
        pis.put(var.getName(), pi);
      }
      pi.addVariableExpression(var);
    }
    return new ArrayList<SQLParameterInfo>(pis.values());
  }

  private List<VariableExpression> determineVariables(DBData dbData) {
    List<VariableExpression> vars = findDescendents(VariableExpression.class);
    Collections.sort(vars, new Comparator<VariableExpression>() {
      @Override
      public int compare(VariableExpression v1, VariableExpression v2) {
        return v1.getStart() - v2.getStart();
      }
    });
    return vars;
  }

  public List<SQLParameterInfo> getParameters() {
    return _parameters;
  }

  @Override
  public String getDefaultTableName() {
    return _tableExpr.getFrom().getTableRefs().get(0).getName().getValue(); //TODO cgross - support multiple tables in table clause
  }

  public List<VariableExpression> getVariables() {
    return _variables;
  }
}
