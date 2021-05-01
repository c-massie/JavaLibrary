package scot.massie.lib.maths;

import com.sun.istack.internal.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

public class EquationEvaluator
{
    //region inner classes
    public static final class Builder
    {
        String unparsedEquation;

        Map<List<Token>, Operator> infixOperators;
        Map<Token, PrefixOperator> prefixOperators;
        Map<Token, PostfixOperator> postfixOperators;
        Map<String, ToDoubleFunction<double[]>> functions;
        Map<String, Double> variables;

        public Builder(@NotNull String equation)
        { this(equation, true); }

        public Builder(@NotNull String equation, boolean includeDefaults)
        {
            this(equation,
                 includeDefaults ? getDefaultInfixOperators()   : new HashMap<>(),
                 includeDefaults ? getDefaultPrefixOperators()  : new HashMap<>(),
                 includeDefaults ? getDefaultPostfixOperators() : new HashMap<>(),
                 includeDefaults ? getDefaultFunctions()        : new HashMap<>(),
                 includeDefaults ? getDefaultVariables()        : new HashMap<>());
        }

        private Builder(String equation,
                        Map<List<Token>, Operator> infixOperators,
                        Map<Token, PrefixOperator> prefixOperators,
                        Map<Token, PostfixOperator> postfixOperators,
                        Map<String, ToDoubleFunction<double[]>> functions,
                        Map<String, Double> variables)
        {
            if(equation == null)
                throw new NullPointerException("Equations cannot be null strings.");

            this.unparsedEquation = equation;
            this.infixOperators = infixOperators;
            this.prefixOperators = prefixOperators;
            this.postfixOperators = postfixOperators;
            this.functions = functions;
            this.variables = variables;
        }

        private static Map<List<Token>, Operator> getDefaultInfixOperators()
        {
            return new HashMap<>();
        }

        private static Map<Token, PrefixOperator> getDefaultPrefixOperators()
        {
            return new HashMap<>();
        }

        private static Map<Token, PostfixOperator> getDefaultPostfixOperators()
        {
            return new HashMap<>();
        }

        private static Map<String, ToDoubleFunction<double[]>> getDefaultFunctions()
        {
            return new HashMap<>();
        }

        private static Map<String, Double> getDefaultVariables()
        {
            return new HashMap<>();
        }

        public Builder withVariable(String name, double value)
        {
            variables.put(name, value);
            return this;
        }

        public Builder withFunction(String name, ToDoubleFunction<double[]> f)
        {
            functions.put(name, f);
            return this;
        }

        public Builder withPrefixOperator(String token, UnaryOperatorAction action)
        {
            Token t = new Token(token);
            prefixOperators.put(t, new PrefixOperator(t, action));
            return this;
        }

        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        {
            Token t = new Token(token);
            postfixOperators.put(t, new PostfixOperator(t, action));
            return this;
        }

        public Builder withOperator(String token, BinaryOperatorAction action)
        {
            Token t = new Token(token);
            infixOperators.put(Arrays.asList(t), new BinaryOperator(t, action));
            return this;
        }

        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        {
            Token t1 = new Token(token1);
            Token t2 = new Token(token2);
            infixOperators.put(Arrays.asList(t1, t2), new TernaryOperator(t1, t2, action));
            return this;
        }

        public Builder withOperator(String[] tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.length);

            for(String i : tokens)
                ts.add(new Token(i));

            infixOperators.put(ts, new InfixOperator(ts, action));
            return this;
        }

        public Builder withOperator(List<String> tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.size());

            for(String i : tokens)
                ts.add(new Token(i));

            infixOperators.put(ts, new InfixOperator(ts, action));
            return this;
        }

        public EquationEvaluator build()
        {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private static class Tokeniser
    {
        List<Token> tokens;

        public Tokeniser(List<Token> tokens)
        { this.tokens = tokens; }

        public List<Token> tokenise(String s)
        {
            List<Token> result = new ArrayList<>();
            result.add(new UntokenisedString(s.trim()));

            for(Token t : tokens)
            {
                String tStringEscaped = Pattern.quote(t.toString());

                for(int i = 0; i < result.size(); i++)
                {
                    Token rt = result.get(i);

                    if(!(rt instanceof UntokenisedString))
                        continue;

                    String rtString = rt.toString();
                    String[] rtSplit = rtString.split(tStringEscaped);

                    if(rtSplit.length > 1)
                    {
                        List<Token> rtTokens = new ArrayList<>(rtSplit.length * 2 - 1);
                        int rtSplitLastIndex = rtSplit.length - 1;

                        for(int j = 0; j < rtSplitLastIndex; j++)
                        {
                            rtTokens.add(new UntokenisedString(rtSplit[j].trim()));
                            rtTokens.add(t);
                        }

                        rtTokens.add(new UntokenisedString(rtSplit[rtSplitLastIndex].trim()));

                        result.remove(i);
                        result.addAll(i, rtTokens);
                        i += rtTokens.size() - 1;
                    }
                }
            }

            tokeniseNumbers(result);
            return result;
        }

        /**
         * Replaces all {@link UntokenisedString untokenised strings} in a list of tokens that are parseable as numbers
         * with {@link NumberToken number tokens}.
         * @param tokens The list of tokens to parse the numbers in.
         */
        private void tokeniseNumbers(List<Token> tokens)
        {
            for(int i = 0; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(!(itoken instanceof UntokenisedString))
                    continue;

                String itokenText = itoken.toString();
                double asNumber;

                try
                { asNumber = Double.parseDouble(itokenText); }
                catch(NumberFormatException e)
                { continue; }

                tokens.remove(i);
                tokens.add(i, new NumberToken(itokenText, asNumber));
            }
        }
    }

    //region Tokens
    private static class Token
    {
        public static final Token OPEN_BRACKET = new Token("(");
        public static final Token CLOSE_BRACKET = new Token(")");

        final String text;

        public Token(@NotNull String asText)
        { this.text = asText; }

        @Override
        public String toString()
        { return text; }

        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;

            if(o == null || getClass() != o.getClass())
                return false;

            Token other = (Token)o;
            return text.equals(other.text);
        }

        @Override
        public int hashCode()
        { return text.hashCode(); }
    }

    private static class UntokenisedString extends Token
    {
        public UntokenisedString(String s)
        { super(s); }
    }

    private static class NumberToken extends Token
    {
        public NumberToken(String asText, double asNumber)
        {
            super(asText);
            this.value = asNumber;
        }

        final double value;

        public double getValue()
        { return value; }
    }
    //endregion

    //region operators
    private static class Operator
    {
        public Operator(Token token, OperatorAction action)
        { this(Arrays.asList(token), action); }

        public Operator(Token a, Token b, OperatorAction action)
        { this(Arrays.asList(a, b), action); }

        public Operator(Token[] tokens, OperatorAction action)
        { this(Arrays.asList(tokens), action); }

        public Operator(List<Token> tokens, OperatorAction action)
        {
            this.tokens = tokens;
            this.action = action;
        }

        final List<Token> tokens;
        final OperatorAction action;
    }

    private static class UnaryOperator extends Operator
    {
        public UnaryOperator(Token token, UnaryOperatorAction action)
        { super(token, operands -> action.performOperation(operands[0])); }
    }

    private static class PrefixOperator extends UnaryOperator
    {
        public PrefixOperator(Token token, UnaryOperatorAction action)
        { super(token, action); }
    }

    private static class PostfixOperator extends UnaryOperator
    {
        public PostfixOperator(Token token, UnaryOperatorAction action)
        { super(token, action); }
    }

    private static class InfixOperator extends Operator
    {
        public InfixOperator(Token token, OperatorAction action)
        { super(token, action); }

        public InfixOperator(Token a, Token b, OperatorAction action)
        { super(a, b, action); }

        public InfixOperator(Token[] tokens, OperatorAction action)
        { super(tokens, action); }

        public InfixOperator(List<Token> tokens, OperatorAction action)
        { super(tokens, action); }
    }

    private static class BinaryOperator extends InfixOperator
    {
        public BinaryOperator(Token token, BinaryOperatorAction action)
        { super(token, operands -> action.performOperation(operands[0], operands[1])); }
    }

    private static class TernaryOperator extends InfixOperator
    {
        public TernaryOperator(Token a, Token b, TernaryOperatorAction action)
        { super(a, b, operands -> action.performOperation(operands[0], operands[1], operands[2])); }
    }
    //endregion

    //region equation components
    private static abstract class EquationComponent
    {
        public abstract double evaluate();
    }

    private static class Operation extends EquationComponent
    {
        EquationComponent[] components;
        OperatorAction action;

        @Override
        public double evaluate()
        {
            double[] operands = new double[components.length];

            for(int i = 0; i < components.length; i++)
                operands[i] = components[i].evaluate();

            return action.performOperation(operands);
        }
    }

    private static class FunctionCall extends EquationComponent
    {
        public FunctionCall(Map<String, ToDoubleFunction<double[]>> functionMap,
                            String functionName,
                            EquationComponent... arguments)
        {
            this.functionMap = functionMap;
            this.functionName = functionName;
            this.arguments = arguments;
        }

        String functionName;
        EquationComponent[] arguments;
        Map<String, ToDoubleFunction<double[]>> functionMap;

        @Override
        public double evaluate()
        {
            ToDoubleFunction<double[]> f = functionMap.get(functionName);

            if(f == null)
                throw new EquationEvaluation.MissingFunctionException(functionName);

            double[] results = new double[arguments.length];

            for(int i = 0; i < results.length; i++)
                results[i] = arguments[i].evaluate();

            return f.applyAsDouble(results);
        }
    }

    private static class VariableReference extends EquationComponent
    {
        public VariableReference(Map<String, Double> variableValues, String name)
        {
            this.variableValues = variableValues;
            this.name = name;
        }

        Map<String, Double> variableValues;
        String name;

        @Override
        public double evaluate()
        {
            Double result = variableValues.get(name);

            if(result == null)
                throw new EquationEvaluation.MissingVariableException(name);

            return result;
        }
    }

    private static class LiteralNumber extends EquationComponent
    {
        public LiteralNumber(double value)
        { this.value = value; }

        double value;

        @Override
        public double evaluate()
        { return value; }
    }
    //endregion

    //region actions
    @FunctionalInterface
    public interface OperatorAction
    {
        double performOperation(double... operands);
    }

    @FunctionalInterface
    public interface UnaryOperatorAction
    {
        double performOperation(double operand);
    }

    @FunctionalInterface
    public interface BinaryOperatorAction
    {
        double performOperation(double a, double b);
    }

    @FunctionalInterface
    public interface TernaryOperatorAction
    {
        double performOperation(double a, double b, double c);
    }
    //endregion
    //endregion

    //region variables
    final EquationComponent topLevelComponent;
    //endregion

    //region initialisation
    private EquationEvaluator(EquationComponent topLevelComponent)
    {
        this.topLevelComponent = topLevelComponent;
    }
    //endregion

    //region methods

    //endregion
}
