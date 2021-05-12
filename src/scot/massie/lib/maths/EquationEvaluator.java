package scot.massie.lib.maths;

import com.sun.istack.internal.NotNull;
import scot.massie.lib.collections.tree.RecursiveTree;
import scot.massie.lib.collections.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static scot.massie.lib.utils.ControlFlowUtils.*;

public class EquationEvaluator
{
    //region inner classes
    public static final class Builder
    {
        //region inner classes
        //region exceptions
        public static class EquationParseException extends RuntimeException
        {
            final TokenList equationSection;
            final TokenList fullEquation;

            EquationParseException(TokenList fullEquation, TokenList equationSection)
            {
                super("Equation was not parsable as an equation: " + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);

                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            EquationParseException(TokenList fullEquation, TokenList equationSection, String msg)
            {
                super(msg);
                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            public String getEquationSection()
            { return equationSection.equationAsString.trim(); }

            public String getFullEquation()
            { return fullEquation.equationAsString.trim(); }

            public EquationParseException withFullEquation(TokenList fullEquation)
            { return new EquationParseException(fullEquation, equationSection); }
        }

        public static class DanglingOperatorException extends EquationParseException
        {
            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a dangling operator that could not be a prefix nor postfix operator: "
                        + fullEquation.equationAsString
                        + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingOperatorException withFullEquation(TokenList fullEquation)
            { return new DanglingOperatorException(fullEquation, equationSection); }
        }

        public static class LeadingNonPrefixOperatorException extends DanglingOperatorException
        {
            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a leading operator that could not be a prefix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingNonPrefixOperatorException withFullEquation(TokenList fullEquation)
            { return new LeadingNonPrefixOperatorException(fullEquation, equationSection); }
        }

        public static class TrailingNonPostfixOperatorException extends DanglingOperatorException
        {
            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a trailing operator that could not be a postfix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingNonPostfixOperatorException withFullEquation(TokenList fullEquation)
            { return new TrailingNonPostfixOperatorException(fullEquation, equationSection); }
        }

        public static class DanglingArgumentSeparatorException extends EquationParseException
        {
            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a dangling separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new DanglingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class LeadingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {
            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a leading separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new LeadingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class TrailingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {

            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a trailing separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new TrailingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class BracketMismatchException extends EquationParseException
        {
            public BracketMismatchException(TokenList equation)
            { super(equation, equation, "Equation contained a bracket mismatch: " + equation.equationAsString); }

            public BracketMismatchException(TokenList equation, String msg)
            { super(equation, equation, msg); }

            @Override
            public BracketMismatchException withFullEquation(TokenList fullEquation)
            { return new BracketMismatchException(fullEquation); }
        }

        public static class UnexpectedCloseBracketException extends BracketMismatchException
        {
            public UnexpectedCloseBracketException(TokenList equation)
            {
                super(equation, "Equation contained a close bracket that didn't correlate to a matching open bracket: "
                                + equation.equationAsString);
            }

            public UnexpectedCloseBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnexpectedCloseBracketException withFullEquation(TokenList fullEquation)
            { return new UnexpectedCloseBracketException(fullEquation); }
        }

        public static class UnmatchedOpenBracketException extends BracketMismatchException
        {
            public UnmatchedOpenBracketException(TokenList equation)
            {
                super(equation, "Equation contained an open bracket that didn't correlate to a matching close bracket: "
                                + equation.equationAsString);
            }

            public UnmatchedOpenBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnmatchedOpenBracketException withFullEquation(TokenList fullEquation)
            { return new UnmatchedOpenBracketException(fullEquation); }
        }
        //endregion

        static class OperatorPriorityGroup
        {
            final Map<Token, PrefixOperator> prefixOperators = new HashMap<>();
            final Map<Token, PostfixOperator> postfixOperators = new HashMap<>();
            final Tree<Token, InfixOperator> leftAssociativeInfixOperators = new RecursiveTree<>();
            final Tree<Token, InfixOperator> rightAssociativeInfixOperators = new RecursiveTree<>();
        }

        static class OperatorTokenRun
        {
            int startIndexInSource;
            int endIndexInSource;
            int indexOfPivotInRun;
            List<Token> tokens;
            List<Token> tokensBeforePivot;
            List<Token> tokensAfterPivot;

            public OperatorTokenRun(List<Token> source, int runStartIndex, int runEndIndex, int pivotIndexInSource)
            {
                this.startIndexInSource = runStartIndex;
                this.endIndexInSource = runEndIndex;
                this.indexOfPivotInRun = pivotIndexInSource + runStartIndex;
                this.tokens = source.subList(runStartIndex, runEndIndex + 1);
                this.tokensBeforePivot = source.subList(runStartIndex, pivotIndexInSource);
                this.tokensAfterPivot = source.subList(pivotIndexInSource + 1, runEndIndex);
            }
        }
        //endregion

        double DEFAULT_PRIORITY = 0;
        boolean DEFAULT_ASSOCIATIVITY = true; // true == left, false == right.

        String unparsedEquation;

        /**
         * Fixed string tokens that may appear in an equation. Does not include variable names, function names,
         * numbers, or unparsable portions of equation.
         */
        Set<Token> possibleTokens = new HashSet<>();
        {
            possibleTokens.add(Token.OPEN_BRACKET);
            possibleTokens.add(Token.CLOSE_BRACKET);
            possibleTokens.add(Token.ARGUMENT_SEPARATOR);
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
            possibleTokensInOrder.add(Token.ARGUMENT_SEPARATOR);
        }

        Set<Token> operatorTokens = new HashSet<>();

        Set<Token> infixOperatorTokens = new HashSet<>();

        Map<List<Token>, InfixOperator> infixOperators;
        Map<Token, PrefixOperator> prefixOperators;
        Map<Token, PostfixOperator> postfixOperators;
        Map<String, ToDoubleFunction<double[]>> functions;
        Map<String, Double> variables;

        Map<Double, OperatorPriorityGroup> operatorGroups = null;
        List<OperatorPriorityGroup> operatorGroupsInOrder = null;

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

        Builder(String equation,
                Map<List<Token>, InfixOperator> infixOperators,
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

        static Map<List<Token>, InfixOperator> getDefaultInfixOperators()
        {
            return new HashMap<>();
        }

        static Map<Token, PrefixOperator> getDefaultPrefixOperators()
        {
            return new HashMap<>();
        }

        static Map<Token, PostfixOperator> getDefaultPostfixOperators()
        {
            return new HashMap<>();
        }

        static Map<String, ToDoubleFunction<double[]>> getDefaultFunctions()
        {
            return new HashMap<>();
        }

        static Map<String, Double> getDefaultVariables()
        {
            return new HashMap<>();
        }

        void addOperator(Operator op)
        {
            invalidateOperatorGroups();

            if(op instanceof PrefixOperator)
            {
                PrefixOperator pop = (PrefixOperator)op;
                Token popToken = pop.getToken();
                prefixOperators.put(popToken, pop);
                addOperatorToken(popToken);
            }
            else if(op instanceof PostfixOperator)
            {
                PostfixOperator pop = (PostfixOperator)op;
                Token popToken = pop.getToken();
                postfixOperators.put(popToken, pop);
                addOperatorToken(popToken);
            }
            else if(op instanceof InfixOperator)
            {
                InfixOperator iop = (InfixOperator)op;
                List<Token> iopTokens = iop.getTokens();
                infixOperators.put(iopTokens, iop);
                addOperatorTokens(iopTokens);
                infixOperatorTokens.addAll(iopTokens);
            }
            else
            { throw new UnsupportedOperationException("Unrecognised operator type: " + op.getClass().getName()); }
        }

        void addOperatorToken(Token token)
        {
            if(possibleTokens.add(token))
            {
                possibleTokensInOrder.add(token);
                operatorTokens.add(token);
            }
        }

        void addOperatorTokens(List<Token> tokens)
        {
            for(Token token : tokens)
                if(possibleTokens.add(token))
                {
                    possibleTokensInOrder.add(token);
                    operatorTokens.add(token);
                }
        }

        public Builder withToken(String token)
        {
            addOperatorToken(new Token(token));
            return this;
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
            addOperator(new PrefixOperator(new Token(token), DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        {
            addOperator(new PostfixOperator(new Token(token), DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String token, BinaryOperatorAction action)
        {
            addOperator(new BinaryOperator(new Token(token), DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        {
            addOperator(new TernaryOperator(new Token(token1), new Token(token2), DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String[] tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.length);

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(List<String> tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.size());

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        void buildOperatorGroups()
        {
            if(operatorGroups != null)
                return;

            operatorGroups = new HashMap<>();

            for(Map.Entry<Token, PrefixOperator> e : prefixOperators.entrySet())
            {
                operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                              .prefixOperators
                              .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<Token, PostfixOperator> e : postfixOperators.entrySet())
            {
                operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                              .postfixOperators
                              .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<List<Token>, InfixOperator> e : infixOperators.entrySet())
            {
                if(e.getValue().isLeftAssociative)
                    operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                                  .leftAssociativeInfixOperators
                                  .setAt(e.getValue(), e.getValue().tokens);
                else
                    operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                                  .rightAssociativeInfixOperators
                                  .setAt(e.getValue(), e.getValue().tokens);
            }

            operatorGroupsInOrder = operatorGroups.entrySet()
                                                  .stream()
                                                  .sorted(Map.Entry.comparingByKey())
                                                  .map(Map.Entry::getValue)
                                                  .collect(Collectors.toList());
        }

        void invalidateOperatorGroups()
        {
            operatorGroups = null;
            operatorGroupsInOrder = null;
        }

        public EquationEvaluator build()
        {
            buildOperatorGroups();
            Tokeniser tokeniser = new Tokeniser(possibleTokensInOrder);
            TokenList tokenisation = tokeniser.tokenise(unparsedEquation).unmodifiable();
            verifyTokenisationBrackets(tokenisation);
            EquationComponent topLevelComponent;

            try
            { topLevelComponent = tryParse(tokenisation); }
            catch(EquationParseException e)
            { throw e.withFullEquation(tokenisation); }

            EquationEvaluator result = new EquationEvaluator(topLevelComponent, variables, functions);
            variables = new HashMap<>();
            functions = new HashMap<>();
            return result;
        }

        void verifyTokenisationBrackets(TokenList tokenisation)
        {
            int bracketDepth = 0;

            for(Token t : tokenisation.tokens)
            {
                if(t.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(t.equals(Token.CLOSE_BRACKET))
                {
                    if(--bracketDepth < 0)
                        throw new UnexpectedCloseBracketException(tokenisation);
                }
            }

            if(bracketDepth > 0)
                throw new UnmatchedOpenBracketException(tokenisation);
        }

        EquationComponent tryParse(TokenList tokenisation)
        {
            if(startsWithNonPrefixOperator(tokenisation))
                throw new LeadingNonPrefixOperatorException(tokenisation, tokenisation);

            if(endsWithNonPostfixOperator(tokenisation))
                throw new TrailingNonPostfixOperatorException(tokenisation, tokenisation);

            if(tokenisation.isInBrackets())
                return tryParse(tokenisation.withoutFirstAndLast());

            return nullCoalesce(() -> tryParseVariable(tokenisation),
                                () -> tryParseFunctionCall(tokenisation),
                                () -> tryParseOperation(tokenisation),
                                () -> tryParseNumber(tokenisation),
                                () -> { throw new EquationParseException(tokenisation, tokenisation); });
        }

        boolean startsWithNonPrefixOperator(TokenList tokenList)
        {
            Token first = tokenList.first();
            return operatorTokens.contains(first) && !prefixOperators.containsKey(first);
        }

        boolean endsWithNonPostfixOperator(TokenList tokenList)
        {
            Token last = tokenList.last();
            return operatorTokens.contains(last) && !postfixOperators.containsKey(last);
        }

        Operation tryParseOperation(TokenList tokenList)
        {
            /*

            Notes:
             - Where left- and right-associative operators share the same priority, right associative operators are
               tested first. (and therefore left associative operators are more "sticky")
             - The order of infix operators of the same associativity and priority, is determined by the order in which
               their tokens appear, from left-to-right for right-associative operators and vice versa for
               left-associative.
             */

            for(OperatorPriorityGroup opGroup : operatorGroupsInOrder)
            {
                Operation o = tryParseOperation(tokenList, opGroup);

                if(o != null)
                    return o;
            }

            return null;
        }

        Operation tryParseOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            return nullCoalesce(() -> tryParseInfixOperation_rightAssociative(tokenList, opGroup),
                                () -> tryParseInfixOperation_leftAssociative(tokenList, opGroup),
                                () -> tryParsePrefixOperation(tokenList, opGroup),
                                () -> tryParsePostfixOperation(tokenList, opGroup),
                                () -> null);
        }

        Operation tryParseInfixOperation_rightAssociative(TokenList tokenList,
                                                                  OperatorPriorityGroup opGroup)
        {
            return tryParseInfixOperation_rightAssociative(tokenList,
                                                           opGroup.rightAssociativeInfixOperators,
                                                           -1,
                                                           Collections.emptyList());
        }

        Operation tryParseInfixOperation_rightAssociative(TokenList tokenList,
                                                                  Tree<Token, InfixOperator> ops,
                                                                  int currentlyUpTo,
                                                                  List<Integer> indicesOfOperatorTokens)
        {
            if(ops.isEmpty())
                return null;

            if(ops.hasRootItem())
                return ops.getRootItem().tryParseFromSplits(tokenList, indicesOfOperatorTokens, this);

            int bracketDepth = 0;

            for(int i = currentlyUpTo + 1; i < tokenList.size(); i++)
            {
                Token itoken = tokenList.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0
                     && infixOperatorTokens.contains(itoken)
                     && canBeInfixOperatorToken(tokenList.tokens, i)
                     && ops.hasItemsAtOrUnder(itoken))
                {
                    List<Integer> newIndices = new ArrayList<>(indicesOfOperatorTokens);
                    newIndices.add(i);
                    Operation o
                            = tryParseInfixOperation_rightAssociative(tokenList, ops.getBranch(itoken), i, newIndices);

                    if(o != null)
                        return o;
                }
            }

            return null;
        }

        Operation tryParseInfixOperation_leftAssociative(TokenList tokenList,
                                                                  OperatorPriorityGroup opGroup)
        {
            return tryParseInfixOperation_leftAssociative(tokenList,
                                                          opGroup.leftAssociativeInfixOperators.withReversedKeys(),
                                                          tokenList.size(),
                                                          Collections.emptyList());
        }

        Operation tryParseInfixOperation_leftAssociative(TokenList tokenList,
                                                                 Tree<Token, InfixOperator> ops,
                                                                 int currentlyDownTo,
                                                                 List<Integer> indicesOfOperatorTokens)
        {
            if(ops.isEmpty())
                return null;

            if(ops.hasRootItem())
                return ops.getRootItem().tryParseFromSplits(tokenList, indicesOfOperatorTokens, this);

            int bracketDepth = 0;

            for(int i = currentlyDownTo - 1; i >= 0; i--)
            {
                Token itoken = tokenList.get(i);

                if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0
                     && infixOperatorTokens.contains(itoken)
                     && canBeInfixOperatorToken(tokenList.tokens, i)
                     && ops.hasItemsAtOrUnder(itoken))
                {
                    List<Integer> newIndices = new ArrayList<>(indicesOfOperatorTokens);
                    newIndices.add(i);
                    Operation o
                            = tryParseInfixOperation_leftAssociative(tokenList, ops.getBranch(itoken), i, newIndices);

                    if(o != null)
                        return o;
                }
            }

            return null;
        }

        Operation tryParsePrefixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PrefixOperator op = opGroup.prefixOperators.get(tokenList.first());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }

        Operation tryParsePostfixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PostfixOperator op = opGroup.postfixOperators.get(tokenList.last());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }

        VariableReference tryParseVariable(TokenList tokenList)
        {
            Double variableValue = variables.get(tokenList.equationAsString.trim());
            return variableValue == null ? null : new VariableReference(variables, tokenList.equationAsString);
        }

        FunctionCall tryParseFunctionCall(TokenList tokenList)
        {
            if(tokenList.size() < 3) // Needs at least 3 tokens: a name, "(", and ")".
                return null;

            if(!tokenList.last().equals(Token.CLOSE_BRACKET))
                return null;

            int lastIndexToCheck = tokenList.size() - 2;
            String functionName = null;
            TokenList argListTokenList = null;

            for(int i = 1; i <= lastIndexToCheck; i++)
            {
                if(tokenList.get(i).equals(Token.OPEN_BRACKET))
                {
                    functionName = tokenList.subList(0, i).equationAsString.trim();
                    argListTokenList = tokenList.subList(i + 1, tokenList.size() - 1);
                    break;
                }
            }

            if(argListTokenList == null)
                return null;

            if(argListTokenList.startsWith(Token.ARGUMENT_SEPARATOR))
                throw new LeadingArgumentSeparatorException(tokenList, tokenList);

            if(argListTokenList.endsWith(Token.ARGUMENT_SEPARATOR))
                throw new TrailingArgumentSeparatorException(tokenList, tokenList);

            List<TokenList> argTokenLists = argListTokenList.splitBy(Token.ARGUMENT_SEPARATOR);
            EquationComponent[] arguments = new EquationComponent[argTokenLists.size()];

            for(int i = 0; i < argTokenLists.size(); i++)
                arguments[i] = tryParse(argTokenLists.get(i));

            return new FunctionCall(functions, functionName, arguments);
        }

        LiteralNumber tryParseNumber(TokenList tokenList)
        {
            try
            {
                double literalNumberValue = Double.parseDouble(tokenList.equationAsString.trim());
                return new LiteralNumber(literalNumberValue);
            }
            catch(NumberFormatException e)
            { return null; }
        }

        // Tokens in a tokenised equation may only be infix operators where:
        //  - There are tokens before and after the run.
        //  - All tokens in the run of operator tokens it's in before it may be postfix operators
        //  - All tokens in the run of operator tokens it's in after it may be prefix operators
        boolean canBeInfixOperatorToken(List<Token> tokens, int tokenIndex)
        {
            if(tokenIndex == 0 || tokenIndex == tokens.size() - 1)
                return false;

            OperatorTokenRun run = getOpRun(tokens, tokenIndex);

            if((run.startIndexInSource == 0) || (run.endIndexInSource == tokens.size() - 1))
                return false;

            for(Token i : run.tokensBeforePivot)
                if(!postfixOperators.containsKey(i))
                    return false;

            for(Token i : run.tokensAfterPivot)
                if(!prefixOperators.containsKey(i))
                    return false;

            return true;
        }

        // idk, I'm bad at naming things.
        OperatorTokenRun getOpRun(List<Token> tokens, int indexToGetOpRunThatContainsIt)
        {
            int min = -1, max = -1;

            for(int i = indexToGetOpRunThatContainsIt - 1; i > 0; i--)
            {
                Token itoken = tokens.get(i);

                if(!operatorTokens.contains(itoken))
                {
                    min = i + 1;
                    break;
                }
            }

            for(int i = indexToGetOpRunThatContainsIt + 1; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(!operatorTokens.contains(itoken))
                {
                    max = i - 1;
                    break;
                }
            }

            if(min == -1)
                min = 0;

            if(max == -1)
                max = tokens.size() - 1;

            return new OperatorTokenRun(tokens, min, max, indexToGetOpRunThatContainsIt);
        }
    }

    static class Tokeniser
    {
        List<Token> tokens;

        public Tokeniser(List<Token> tokens)
        { this.tokens = tokens; }

        public TokenList tokenise(String s)
        {
            String sTrimmed = s.trim();

            if(sTrimmed.isEmpty())
                return new TokenList(s, Collections.emptyList(), Collections.singletonList(s.length()));

            LinkedList<Token> result = new LinkedList<>();
            LinkedList<Integer> spacesBeforeTokens = new LinkedList<>();

            spacesBeforeTokens.add(countSpacesAtStart(s));
            result.add(new UntokenisedString(sTrimmed));
            spacesBeforeTokens.add(countSpacesAtEnd(s));

            for(Token tokenToSplitOn : tokens)
            {
                ListIterator<Token> resultIterator = result.listIterator();
                ListIterator<Integer> spacingsIterator = spacesBeforeTokens.listIterator();

                while(resultIterator.hasNext())
                {
                    Token tokenToSplit = resultIterator.next();
                    spacingsIterator.next();

                    if(!(tokenToSplit instanceof UntokenisedString))
                        continue;

                    TokenList tokenSplit = tokeniseStringWithSingleToken(tokenToSplit.text, tokenToSplitOn);

                    resultIterator.remove();

                    for(Token subtoken : tokenSplit.tokens)
                        resultIterator.add(subtoken);

                    // First and last spacing should be ignored; should be 0 on both counts, as the spaces have
                    // already been accounted for and are in spacesBeforeTokens.
                    int lastSpacingIndexToAdd = tokenSplit.spacings.size() - 2;

                    for(int i = 1; i <= lastSpacingIndexToAdd; i++)
                        spacingsIterator.add(tokenSplit.spacings.get(i));
                }
            }

            tokeniseNumbers(result);
            return new TokenList(s, result, spacesBeforeTokens);
        }

        static TokenList tokeniseStringWithSingleToken(String tokenText, Token tokenToSplitBy)
        {
            String textToSplitBy = tokenToSplitBy.text;
            LinkedList<Token> resultTokens = new LinkedList<>();
            LinkedList<Integer> resultSpacings = new LinkedList<>();

            int indexOfSplitter = tokenText.indexOf(textToSplitBy);
            int indexOfNextPortion = 0;

            while(indexOfSplitter != -1)
            {
                String nonSplitterPortion = tokenText.substring(indexOfNextPortion, indexOfSplitter);
                String nonSplitterPortionTrimmed = nonSplitterPortion.trim();

                if(nonSplitterPortionTrimmed.isEmpty())
                    resultSpacings.add(nonSplitterPortion.length());
                else
                {
                    resultSpacings.add(countSpacesAtStart(nonSplitterPortion));
                    resultTokens.add(new UntokenisedString(nonSplitterPortionTrimmed));
                    resultSpacings.add(countSpacesAtEnd(nonSplitterPortion));
                }

                resultTokens.add(tokenToSplitBy);

                indexOfNextPortion = indexOfSplitter + textToSplitBy.length();
                indexOfSplitter = tokenText.indexOf(textToSplitBy, indexOfNextPortion);
            }

            String finalNonSplitterPortion = tokenText.substring(indexOfNextPortion);
            String finalNonSplitterPortionTrimmed = finalNonSplitterPortion.trim();

            if(finalNonSplitterPortionTrimmed.isEmpty())
                resultSpacings.add(finalNonSplitterPortion.length());
            else
            {
                resultSpacings.add(countSpacesAtStart(finalNonSplitterPortion));
                resultTokens.add(new UntokenisedString(finalNonSplitterPortionTrimmed));
                resultSpacings.add(countSpacesAtEnd(finalNonSplitterPortion));
            }

            return new TokenList(tokenText, resultTokens, resultSpacings);
        }

        /**
         * Replaces all {@link UntokenisedString untokenised strings} in a list of tokens that are parseable as numbers
         * with {@link NumberToken number tokens}.
         * @param tokens The list of tokens to parse the numbers in.
         */
        static void tokeniseNumbers(LinkedList<Token> tokens)
        {
            ListIterator<Token> tokenIterator = tokens.listIterator();

            while(tokenIterator.hasNext())
            {
                Token token = tokenIterator.next();

                if(!(token instanceof UntokenisedString))
                    continue;

                String tokenText = token.toString();
                double asNumber;

                try
                { asNumber = Double.parseDouble(tokenText); }
                catch(NumberFormatException e)
                { continue; }

                tokenIterator.set(new NumberToken(tokenText, asNumber));
            }
        }

        static int countSpacesAtStart(@NotNull String s)
        {
            for(int i = 0; i < s.length(); i++)
                if(s.charAt(i) != ' ')
                    return i;

            return s.length();
        }

        static int countSpacesAtEnd(@NotNull String s)
        {
            for(int i = 0, r = s.length() - 1; r >= 0; i++, r--)
                if(s.charAt(r) != ' ')
                    return i;

            return s.length();
        }
    }

    static class TokenList
    {
        public final String equationAsString;
        public final List<Token> tokens;
        public final List<Integer> spacings;

        public TokenList(String equationAsString, List<Token> tokens, List<Integer> spacingList)
        {
            this.equationAsString = equationAsString;
            this.tokens = tokens;
            this.spacings = spacingList;
        }

        public int size()
        { return tokens.size(); }

        public boolean isInBrackets()
        {
            // Assumes that this TokenList doesn't have any bracket mismatches.

            if(!tokens.get(0).equals(Token.OPEN_BRACKET))
                return false;

            if(!tokens.get(tokens.size() - 1).equals(Token.CLOSE_BRACKET))
                return false;

            int maxToCheck = tokens.size() - 2;
            int bracketDepth = 1;

            for(int i = 1; i <= maxToCheck; i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                {
                    if(--bracketDepth == 0)
                        return false;
                }
            }

            return true;
        }

        public Token get(int index)
        { return tokens.get(index); }

        public Token first()
        { return tokens.get(0); }

        public Token last()
        { return tokens.get(tokens.size() - 1); }

        public TokenList unmodifiable()
        {
            List<Token> newTokens = Collections.unmodifiableList(tokens);
            List<Integer> newSpacings = Collections.unmodifiableList(spacings);
            return new TokenList(equationAsString, newTokens, newSpacings);
        }

        public boolean startsWith(Token t)
        { return tokens.get(0).equals(t); }

        public boolean endsWith(Token t)
        { return tokens.get(tokens.size() - 1).equals(t); }

        public TokenList withoutFirst(int howMany)
        {
            if(howMany < 0)
                throw new IllegalArgumentException("howMany < 0");

            if(size() < howMany)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(howMany, tokens.size());
            List<Integer> newSpacings = spacings.subList(howMany, spacings.size());
            int charsToDrop = 0;

            for(int i = 0; i < howMany; i++)
                charsToDrop += tokens.get(i).text.length() + spacings.get(i);

            String newString = equationAsString.substring(charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        public TokenList withoutFirst()
        {
            if(size() == 0)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(1, tokens.size());
            List<Integer> newSpacings = spacings.subList(1, spacings.size());
            int charsToDrop = tokens.get(0).text.length() + spacings.get(0);
            String newString = equationAsString.substring(charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        public TokenList withoutLast(int howMany)
        {
            if(howMany < 0)
                throw new IllegalArgumentException("howMany < 0");

            if(size() < howMany)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(0, tokens.size() - howMany);
            List<Integer> newSpacings = spacings.subList(0, spacings.size() - howMany);
            int charsToDrop = 0;

            for(int i = tokens.size() - 1; i >= tokens.size() - howMany; i--)
                charsToDrop += tokens.get(i).text.length() + spacings.get(i + 1);

            String newString = equationAsString.substring(0, equationAsString.length() - charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        public TokenList withoutLast()
        {
            if(size() == 0)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(0, tokens.size() - 1);
            List<Integer> newSpacings = spacings.subList(0, spacings.size() - 1);
            int charsToDrop = tokens.get(tokens.size() - 1).text.length() + spacings.get(spacings.size() - 1);
            String newString = equationAsString.substring(0, equationAsString.length() - charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        public TokenList withoutFirstAndLast()
        {
            if(size() <= 1)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(1, tokens.size() - 1);
            List<Integer> newSpacings = spacings.subList(1, spacings.size() - 1);
            int charsToDropFromStart = tokens.get(0).text.length() + spacings.get(0);
            int charsToDropFromEnd = tokens.get(tokens.size() - 1).text.length() + spacings.get(spacings.size() - 1);
            String newString = equationAsString.substring(charsToDropFromStart,
                                                          equationAsString.length() - charsToDropFromEnd);

            return new TokenList(newString, newTokens, newSpacings);
        }

        public TokenList subList(int fromInclusive, int toExclusive)
        {
            if(fromInclusive < 0)
                throw new IllegalArgumentException("fromInclusive < 0");

            if(toExclusive > size())
                throw new IllegalArgumentException("toExclusive > size");

            if(fromInclusive > toExclusive)
                throw new IllegalArgumentException("fromInclusive > toExclusive");


            List<Token> newTokens = tokens.subList(fromInclusive, toExclusive);
            List<Integer> newSpacings = spacings.subList(fromInclusive, toExclusive + 1);

            int charsToDropFromStart = 0;
            int charsToDropFromEnd = 0;

            for(int i = 0; i < fromInclusive; i++)
                charsToDropFromStart += spacings.get(i) + tokens.get(i).text.length();

            for(int i = tokens.size() - 1; i >= toExclusive; i--)
                charsToDropFromEnd += spacings.get(i + 1) + tokens.get(i).text.length();

            String newString = equationAsString.substring(charsToDropFromStart,
                                                          equationAsString.length() - charsToDropFromEnd);

            return new TokenList(newString, newTokens, newSpacings);
        }

        public List<TokenList> splitBy(Token t)
        {
            List<TokenList> sublists = new ArrayList<>();
            int lastMatch = -1;
            int bracketDepth = 0;

            for(int i = 0; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(t))
                {
                    sublists.add(subList(lastMatch + 1, i));
                    lastMatch = i;
                }
            }

            sublists.add(subList(lastMatch + 1, size()));
            return sublists;
        }

        public List<TokenList> splitBySequence(List<Token> sequence)
        {
            List<TokenList> result = new ArrayList<>();
            int sequenceIndex = 0;
            Token sequenceToken = sequence.get(sequenceIndex);
            int previousSplitIndex = -1;
            int bracketDepth = 0;

            for(int i = 0; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(sequenceToken))
                {
                    result.add(subList(previousSplitIndex + 1, i));
                    previousSplitIndex = i;

                    sequenceIndex++;

                    if(sequenceIndex == sequence.size())
                        break;

                    sequenceToken = sequence.get(sequenceIndex);
                }
            }

            if(sequenceIndex < sequence.size()) // Not all tokens in sequence were found.
                return null;

            result.add(subList(previousSplitIndex + 1, size()));
            return result;
        }

        public List<TokenList> splitBySequenceInReverse(List<Token> sequence)
        {
            List<TokenList> result = new ArrayList<>();
            int sequenceIndex = sequence.size() - 1;
            Token sequenceToken = sequence.get(sequenceIndex);
            int previousSplitIndex = tokens.size();
            int bracketDepth = 0;

            for(int i = tokens.size() - 1; i >= 0; i--)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(sequenceToken))
                {
                    result.add(subList(i + 1, previousSplitIndex));
                    previousSplitIndex = i;

                    sequenceIndex--;

                    if(sequenceIndex == -1)
                        break;

                    sequenceToken = sequence.get(sequenceIndex);
                }
            }

            if(sequenceIndex >= 0) // Not all tokens in sequence were found.
                return null;

            result.add(subList(0, previousSplitIndex));
            Collections.reverse(result);
            return result;
        }

        public List<TokenList> splitAtPoints(List<Integer> points)
        {
            points = new ArrayList<>(new HashSet<>(points));
            points.sort(Comparator.naturalOrder());
            List<TokenList> result = new ArrayList<>();
            int previousPoint = -1;

            for(int point : points)
            {
                result.add(subList(previousPoint + 1, point));
                previousPoint = point;
            }

            result.add(subList(previousPoint + 1, size()));
            return result;
        }
    }

    //region Tokens
    static class Token
    {
        public static final Token OPEN_BRACKET = new Token("(");
        public static final Token CLOSE_BRACKET = new Token(")");
        public static final Token ARGUMENT_SEPARATOR = new Token(",");

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

    static class UntokenisedString extends Token
    {
        public UntokenisedString(String s)
        { super(s); }
    }

    static class NumberToken extends Token
    {
        final double value;

        public NumberToken(String asText, double asNumber)
        {
            super(asText);
            this.value = asNumber;
        }

        public double getValue()
        { return value; }

        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;

            if(o == null || getClass() != o.getClass())
                return false;

            NumberToken other = (NumberToken)o;
            return text.equals(other.text) && Double.compare(other.value, value) == 0;
        }

        @Override
        public int hashCode()
        { return Objects.hash(super.hashCode(), value); }
    }
    //endregion

    //region operators
    static abstract class Operator
    {
        final List<Token> tokens;
        final double priority;
        final OperatorAction action;

        public Operator(List<Token> tokens, double priority, OperatorAction action)
        {
            this.tokens = Collections.unmodifiableList(tokens);
            this.priority = priority;
            this.action = action;
        }

        public double getPriority()
        { return priority; }

        public List<Token> getTokens()
        { return tokens; }

        public abstract Operation tryParse(TokenList tokenList, Builder builder);
    }

    static abstract class UnaryOperator extends Operator
    {
        public UnaryOperator(Token token, double priority, UnaryOperatorAction action)
        { super(Arrays.asList(token), priority, operands -> action.performOperation(operands[0])); }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    static class PrefixOperator extends UnaryOperator
    {
        public PrefixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            if(tokenList.size() < 2 || !tokenList.first().equals(getToken()))
                return null;

            return new Operation(builder.tryParse(tokenList.withoutFirst()), this.action);
        }
    }

    static class PostfixOperator extends UnaryOperator
    {
        public PostfixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            if(tokenList.size() < 2 || !tokenList.last().equals(getToken()))
                return null;

            return new Operation(builder.tryParse(tokenList.withoutLast()), this.action);
        }
    }

    static class InfixOperator extends Operator
    {
        final boolean isLeftAssociative;

        public InfixOperator(List<Token> tokens, boolean isLeftAssociative, double priority, OperatorAction action)
        {
            super(tokens, priority, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        public boolean isLeftAssociative()
        { return isLeftAssociative; }

        public boolean isRightAssociative()
        { return !isLeftAssociative; }

        public int getOperandCount()
        { return this.tokens.size() + 1; }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            List<TokenList> split = isLeftAssociative ? tokenList.splitBySequenceInReverse(this.tokens)
                                                      : tokenList.splitBySequence(this.tokens);

            return split == null ? null : compileFromOperandTokenLists(split, builder);
        }

        public Operation tryParseFromSplits(TokenList tokenList, List<Integer> splitPoints, Builder builder)
        { return compileFromOperandTokenLists(tokenList.splitAtPoints(splitPoints), builder); }

        Operation compileFromOperandTokenLists(List<TokenList> tokenLists, Builder builder)
        {
            List<EquationComponent> components = new ArrayList<>(tokenLists.size());

            for(TokenList tl : tokenLists)
                components.add(builder.tryParse(tl));

            return new Operation(components, action);
        }
    }

    static class BinaryOperator extends InfixOperator
    {
        public BinaryOperator(Token token, boolean isLeftAssociative, double priority, BinaryOperatorAction action)
        {
            super(Arrays.asList(token),
                  isLeftAssociative,
                  priority,
                  operands -> action.performOperation(operands[0], operands[1]));
        }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    static class TernaryOperator extends InfixOperator
    {
        public TernaryOperator(Token a,
                               Token b,
                               boolean isLeftAssociative,
                               double priority,
                               TernaryOperatorAction action)
        {
            super(Arrays.asList(a, b),
                  isLeftAssociative,
                  priority,
                  operands -> action.performOperation(operands[0], operands[1], operands[2]));
        }

        public Token getLeftToken()
        { return this.tokens.get(0); }

        public Token getRightToken()
        { return this.tokens.get(1); }
    }
    //endregion

    //region equation components
    static abstract class EquationComponent
    { public abstract double evaluate(); }

    static class Operation extends EquationComponent
    {
        List<EquationComponent> components;
        OperatorAction action;

        public Operation(EquationComponent component, OperatorAction action)
        { this(Arrays.asList(component), action); }

        public Operation(EquationComponent component1, EquationComponent component2, OperatorAction action)
        { this(Arrays.asList(component1, component2), action); }

        public Operation(EquationComponent[] components, OperatorAction action)
        { this(Arrays.asList(components), action); }

        public Operation(List<EquationComponent> components, OperatorAction action)
        {
            this.components = components;
            this.action = action;
        }

        @Override
        public double evaluate()
        {
            double[] operands = new double[components.size()];

            for(int i = 0; i < components.size(); i++)
                operands[i] = components.get(i).evaluate();

            return action.performOperation(operands);
        }
    }

    static class FunctionCall extends EquationComponent
    {
        String functionName;
        EquationComponent[] arguments;
        Map<String, ToDoubleFunction<double[]>> functionMap;

        public FunctionCall(Map<String, ToDoubleFunction<double[]>> functionMap,
                            String functionName,
                            EquationComponent... arguments)
        {
            this.functionMap = functionMap;
            this.functionName = functionName;
            this.arguments = arguments;
        }

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

    static class VariableReference extends EquationComponent
    {
        Map<String, Double> variableValues;
        String name;

        public VariableReference(Map<String, Double> variableValues, String name)
        {
            this.variableValues = variableValues;
            this.name = name;
        }

        @Override
        public double evaluate()
        {
            Double result = variableValues.get(name);

            if(result == null)
                throw new EquationEvaluation.MissingVariableException(name);

            return result;
        }
    }

    static class LiteralNumber extends EquationComponent
    {
        double value;

        public LiteralNumber(double value)
        { this.value = value; }

        @Override
        public double evaluate()
        { return value; }
    }
    //endregion

    //region actions
    @FunctionalInterface
    public interface OperatorAction
    { double performOperation(double... operands); }

    @FunctionalInterface
    public interface UnaryOperatorAction
    { double performOperation(double operand); }

    @FunctionalInterface
    public interface BinaryOperatorAction
    { double performOperation(double a, double b); }

    @FunctionalInterface
    public interface TernaryOperatorAction
    { double performOperation(double a, double b, double c); }
    //endregion
    //endregion

    //region variables
    final EquationComponent topLevelComponent;
    final Map<String, Double> variableValues;
    final Map<String, ToDoubleFunction<double[]>> functions;
    //endregion

    //region initialisation
    EquationEvaluator(EquationComponent topLevelComponent,
                      Map<String, Double> variableValues,
                      Map<String, ToDoubleFunction<double[]>> functions)
    {
        this.topLevelComponent = topLevelComponent;
        this.variableValues = variableValues;
        this.functions = functions;
    }
    //endregion

    //region methods
    public double evaluate()
    { return topLevelComponent.evaluate(); }

    public boolean setVariable(String variableName, double newValue)
    {
        if(!variableValues.containsKey(variableName))
            return false;

        variableValues.put(variableName, newValue);
        return true;
    }

    public boolean redefineFunction(String functionName, ToDoubleFunction<double[]> function)
    {
        if(!functions.containsKey(functionName))
            return false;

        functions.put(functionName, function);
        return true;
    }
    //endregion
}
