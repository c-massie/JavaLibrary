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

        /**
         * Fixed string tokens that may appear in an equation. Does not include variable names, function names,
         * numbers, or unparsable portions of equation.
         */
        Set<Token> possibleTokens = new HashSet<>();
        {
            possibleTokens.add(Token.OPEN_BRACKET);
            possibleTokens.add(Token.CLOSE_BRACKET);
        }

        /**
         * {@link #possibleTokens}, but in order from first registered to last registered. This is the reverse of the
         * order in which equations are tokenised. It should be noted that where a token whose text contains the text of
         * other tokens is registered earlier (and thus tokenised later), matching strings will be parsed as the smaller
         * tokens rather than the larger ones, as the smaller ones will be considered first.
         */
        List<Token> possibleTokensInOrder = new ArrayList<>();
        {
            possibleTokensInOrder.add(Token.OPEN_BRACKET);
            possibleTokensInOrder.add(Token.CLOSE_BRACKET);
        }

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

        private void addOperator(Operator op)
        {
            if(op instanceof PrefixOperator)
            {
                PrefixOperator pop = (PrefixOperator)op;
                Token popToken = pop.getToken();
                prefixOperators.put(popToken, pop);
                addToken(popToken);
            }
            else if(op instanceof PostfixOperator)
            {
                PostfixOperator pop = (PostfixOperator)op;
                Token popToken = pop.getToken();
                postfixOperators.put(popToken, pop);
                addToken(popToken);
            }
            else if(op instanceof InfixOperator)
            {
                InfixOperator iop = (InfixOperator)op;
                List<Token> iopTokens = iop.getTokens();
                infixOperators.put(iopTokens, iop);
                addTokens(iopTokens);
            }
            else
            { throw new UnsupportedOperationException("Unrecognised operator type: " + op.getClass().getName()); }
        }

        private void addToken(Token token)
        {
            if(possibleTokens.add(token))
                possibleTokensInOrder.add(token);
        }

        private void addTokens(List<Token> tokens)
        {
            for(Token token : tokens)
                if(possibleTokens.add(token))
                    possibleTokensInOrder.add(token);
        }

        public Builder withToken(String token)
        { addToken(new Token(token)); return this; }

        public Builder withVariable(String name, double value)
        { variables.put(name, value); return this; }

        public Builder withFunction(String name, ToDoubleFunction<double[]> f)
        { functions.put(name, f); return this; }

        public Builder withPrefixOperator(String token, UnaryOperatorAction action)
        { addOperator(new PrefixOperator(new Token(token), action)); return this; }

        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        { addOperator(new PostfixOperator(new Token(token), action)); return this; }

        public Builder withOperator(String token, BinaryOperatorAction action)
        { addOperator(new BinaryOperator(new Token(token), action)); return this; }

        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        { addOperator(new TernaryOperator(new Token(token1), new Token(token2), action)); return this; }

        public Builder withOperator(String[] tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.length);

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, action));
            return this;
        }

        public Builder withOperator(List<String> tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.size());

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, action));
            return this;
        }

        public EquationEvaluator build()
        {
            List<Token> equationTokens = new Tokeniser(possibleTokensInOrder).tokenise(unparsedEquation);

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

            for(int tokenIndex = tokens.size() - 1; tokenIndex >= 0; tokenIndex--)
            {
                Token possibleToken = tokens.get(tokenIndex);
                String possibleTokenTextEscaped = Pattern.quote(possibleToken.toString());

                for(int resultTokenIndex = 0; resultTokenIndex < result.size(); resultTokenIndex++)
                {
                    Token resultToken = result.get(resultTokenIndex);

                    if(!(resultToken instanceof UntokenisedString))
                        continue;

                    String rtString = resultToken.toString();
                    String[] rtSplit = rtString.split(possibleTokenTextEscaped);

                    if(rtSplit.length > 1)
                    {
                        List<Token> rtTokens = new ArrayList<>(rtSplit.length * 2 - 1);
                        int rtSplitLastIndex = rtSplit.length - 1;

                        for(int j = 0; j < rtSplitLastIndex; j++)
                        {
                            rtTokens.add(new UntokenisedString(rtSplit[j].trim()));
                            rtTokens.add(possibleToken);
                        }

                        rtTokens.add(new UntokenisedString(rtSplit[rtSplitLastIndex].trim()));

                        result.remove(resultTokenIndex);
                        result.addAll(resultTokenIndex, rtTokens);
                        resultTokenIndex += rtTokens.size() - 1;
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
            this.tokens = Collections.unmodifiableList(tokens);
            this.action = action;
        }

        final List<Token> tokens;
        final OperatorAction action;

        public List<Token> getTokens()
        { return tokens; }
    }

    private static class UnaryOperator extends Operator
    {
        public UnaryOperator(Token token, UnaryOperatorAction action)
        { super(token, operands -> action.performOperation(operands[0])); }

        public Token getToken()
        { return this.tokens.get(0); }
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
        {
            this(token, true, action);
        }

        public InfixOperator(Token a, Token b, OperatorAction action)
        {
            this(a, b, true, action);
        }

        public InfixOperator(Token[] tokens, OperatorAction action)
        {
            this(tokens, true, action);
        }

        public InfixOperator(List<Token> tokens, OperatorAction action)
        {
            this(tokens, true, action);
        }

        public InfixOperator(Token token, boolean isLeftAssociative, OperatorAction action)
        {
            super(token, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        public InfixOperator(Token a, Token b, boolean isLeftAssociative, OperatorAction action)
        {
            super(a, b, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        public InfixOperator(Token[] tokens, boolean isLeftAssociative, OperatorAction action)
        {
            super(tokens, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        public InfixOperator(List<Token> tokens, boolean isLeftAssociative, OperatorAction action)
        {
            super(tokens, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        final boolean isLeftAssociative;

        public boolean isLeftAssociative()
        { return isLeftAssociative; }

        public boolean isRightAssociative()
        { return !isLeftAssociative; }
    }

    private static class BinaryOperator extends InfixOperator
    {
        public BinaryOperator(Token token, BinaryOperatorAction action)
        { super(token, operands -> action.performOperation(operands[0], operands[1])); }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    private static class TernaryOperator extends InfixOperator
    {
        public TernaryOperator(Token a, Token b, TernaryOperatorAction action)
        { super(a, b, operands -> action.performOperation(operands[0], operands[1], operands[2])); }

        public Token getLeftToken()
        { return this.tokens.get(0); }

        public Token getRightToken()
        { return this.tokens.get(1); }
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
