package info.iconmaster.tnclipse.editor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import info.iconmaster.tnclipse.TnClipse;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.PackageNameContext;

public class TyphonTokenScanner implements ITokenScanner {
	TyphonLexer lexer;
	String input;

	int lastOffset, lastLength;
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		try {
			input = document.get(offset, length);
			lexer = new TyphonLexer(new ANTLRInputStream(input));
			lastOffset = offset;
			lastLength = 0;
		} catch (BadLocationException e) {
			Logger.getGlobal().log(Level.SEVERE, "setRange failed", e);
		}
	}
	
	@Override
	public IToken nextToken() {
		org.antlr.v4.runtime.Token lastToken = lexer.nextToken();
		lastOffset += lastLength;
		lastLength = lastToken.getStopIndex() - lastToken.getStartIndex() + 1;
		
		if (lastToken.getType() == lastToken.EOF) {
			return org.eclipse.jface.text.rules.Token.EOF;
		} else if (lastToken.getType() == TyphonLexer.WHITESPACE) {
			return org.eclipse.jface.text.rules.Token.WHITESPACE;
		} else {
			Color color;
			int style = SWT.NONE;
			
			switch (lastToken.getType()) {
			case TyphonLexer.COMMENT:
			case TyphonLexer.BLOCK_COMMENT:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.comment");
				break;
			case TyphonLexer.CHAR:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.char");
				break;
			case TyphonLexer.DOC_COMMENT:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.doc_comment");
				break;
			case TyphonLexer.NUMBER:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.number");
				break;
			case TyphonLexer.STRING:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.string");
				break;
			case TyphonLexer.UNKNOWN_TOKEN:
			case TyphonLexer.WORD:
				color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
				break;
			default:
				if (lastToken.getText().equals("@") || lastToken.getText().equals("@@")) {
					// it's an annotation; syntax highlighting should consume all tokens to the paren
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.annotation");
					
					TyphonLexer miniLexer = new TyphonLexer(new ANTLRInputStream(input.substring(lastToken.getStartIndex())));
					TyphonParser miniParser = new TyphonParser(new CommonTokenStream(miniLexer));
					
					miniLexer.removeErrorListeners();
					miniParser.removeErrorListeners();
					
					try {
						PackageNameContext rule;
						
						if (lastToken.getText().equals("@")) {
							rule = miniParser.annotation().tnName;
						} else {
							rule = miniParser.globalAnnotation().tnName;
						}
						
						int max = -1;
						
						for (org.antlr.v4.runtime.Token token : rule.tnName) {
							int index = token.getStopIndex();
							if (index > max) {
								max = index;
							}
						}
						
						if (max > -1) {
							lastLength = 1 + max;
						}
					} catch (RecognitionException e) {
						// do nothing
					}
					
					lexer = new TyphonLexer(new ANTLRInputStream(input.substring(lastToken.getStartIndex() + lastLength)));
				} else if (Character.isAlphabetic(lastToken.getText().charAt(0))) {
					// if it starts with a letter, it's a keyword
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.keyword");
					style = SWT.BOLD;
				} else {
					// else, it's a normal symbol
					color = TnClipse.colorManager.getColorFromPreferences("editor.color.default");
				}
			}
			
			return new org.eclipse.jface.text.rules.Token(new TextAttribute(color, null, style));
		}
	}
	
	@Override
	public int getTokenOffset() {
		return lastOffset;
	}
	
	@Override
	public int getTokenLength() {
		return lastLength;
	}
}
