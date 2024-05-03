package org.telegram.telegrise.core.expressions;

/**
 * Changes provided code according to the following rules:
 *
 * <ul>
 *     <li>Replace two consequential single quotation mark <code>''</code> with double quotation mark <code>"</code>. If string expression already started with <code>"</code> — ignore. Examples: </li>
 *     <ul>
 *         <li><code>''Hello'' -> "Hello"</code></li>
 *         <li><code>''Don''t'' -> "Don"t"</code></li>
 *         <li><code>"Don''t" -> "Don''t"</code></li>
 *     </ul>
 * </ul>
 *
 * This class is used as a workaround for XML attributes
 * that implements alternative syntax
 * to represent symbols which otherwise would have to be written by $-entities
 *
 * @see JavaExpressionCompiler
 * @since 0.6.0
 */
public class JavaExpressionPreprocessor {

    /** Applies preprocessor rules listed in the class description
     *
     * @param code string to process
     * @return processed string
     */
    public String process(String code){
        StringBuilder builder = new StringBuilder(code.length());
        boolean skip = false;

        for (int i = 0; i < code.length(); i++){
            if (code.charAt(i) == '"'){
                builder.append("\"");
                skip = !skip;
            } else if (!skip && i < code.length() - 1 && code.charAt(i) == '\'' && code.charAt(i + 1) == '\'') {
                builder.append("\"");
                i++;
            } else {
                builder.append(code.charAt(i));
            }
        }

        return builder.toString();
    }
}
