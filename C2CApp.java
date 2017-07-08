// import ANTLR runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.util.*;

class C2CListener extends CBaseListener 
{
	Stack<StringBuilder> out = new Stack<StringBuilder>();
	CParser parser;
	C2CListener(CParser parser){
		out.push(new StringBuilder(""));
		this.parser = parser;
	}

	int doWhileCheck=0; // 0 doWhile 1 While
	int elseCheck=0; // 0 if 1 if-else
	int intentStair=0; // intentNumber

	@Override public void enterDirectiveDefinition(@NotNull CParser.DirectiveDefinitionContext ctx) {
		if(ctx.getParent() instanceof CParser.IncListContext)
			for (int i=0; i< ctx.getChildCount(); i++)
				out.peek().append(ctx.getChild(i).getText()+"\n");	// Print IncList

		out.peek().append("\n");
	}
	
	@Override public void enterFunction(@NotNull CParser.FunctionContext ctx) { 
		for (int i=0; i < ctx.getChildCount(); i++)
			if (ctx.getChild(i) instanceof TerminalNode)
				out.peek().append(ctx.getChild(i).getText()+" ");		// Print Function without CompoundStmt
	}
	
	@Override public void enterCompoundStmt(@NotNull CParser.CompoundStmtContext ctx) {
		out.peek().append("{\n");
	}

	@Override public void exitCompoundStmt(@NotNull CParser.CompoundStmtContext ctx) {
		intentSetting(1);
		out.peek().append("}\n");
	}

	@Override public void enterStmtList(@NotNull CParser.StmtListContext ctx) {
		intentStair++;
	}

	@Override public void exitStmtList(@NotNull CParser.StmtListContext ctx) {
		intentStair--;
	}

	@Override public void enterCallStmt(@NotNull CParser.CallStmtContext ctx) {
		intentSetting(1);
		for( int i=0; i<ctx.getChildCount(); i++)
			out.peek().append( ctx.getChild(i).getText() );
		out.peek().append("\n");
	}

	@Override public void enterRetStmt(@NotNull CParser.RetStmtContext ctx) {
		intentSetting(1);
		for( int i=0; i<ctx.getChildCount(); i++) {
			if( ctx.getChild(i) instanceof CParser.ExprContext )
				out.peek().append(" ");
			out.peek().append( ctx.getChild(i).getText() );
		}
		out.peek().append("\n");
	}

	@Override public void enterDeclaration(@NotNull CParser.DeclarationContext ctx) {
		intentStair++;
		intentSetting(1);
		for( int i=0; i<ctx.getChildCount() ; i++) {
			if( ctx.getChild(i) instanceof CParser.IdentListContext )
				out.peek().append(" ");
			out.peek().append( ctx.getChild(i).getText() );
		}
		out.peek().append("\n");
		intentStair--;
	}

	@Override public void enterAssignStmt(@NotNull CParser.AssignStmtContext ctx) {
		intentSetting(1);
		for( int i=0; i<ctx.getChildCount() ; i++)
			out.peek().append( ctx.getChild(i).getText() );
		out.peek().append("\n");
	}

	@Override public void enterIfStmt(@NotNull CParser.IfStmtContext ctx) {
		intentSetting(1);
		for(int i=0; i<ctx.getChildCount() ; i++) {
			if( ctx.getChild(i).getText().contentEquals("else") )
				elseCheck = 1;
			if( ctx.getChild(i).getText().contentEquals("else if") )
				elseCheck = 1;
		}
		for(int i=0; i<ctx.getChildCount() ; i++) {
			if( ctx.getChild(i) instanceof CParser.StmtContext )
				break;
			out.peek().append( ctx.getChild(i).getText() );
		}
	}

	@Override public void enterWhileStmt(@NotNull CParser.WhileStmtContext ctx) {
		intentSetting(1);
		for(int i=0; i<ctx.getChildCount() ; i++) {
			if( ctx.getChild(i) instanceof CParser.StmtContext )
				break;
			if( ctx.getChild(i).getText().contentEquals("do") )
				doWhileCheck = 1;
			out.peek().append( ctx.getChild(i).getText() );
		}
	}

	@Override public void enterForStmt(@NotNull CParser.ForStmtContext ctx) {
		intentSetting(1);
		for( int i=0; i<ctx.getChildCount() ; i++ ) {
			if( ctx.getChild(i) instanceof CParser.StmtContext )
				break;
			out.peek().append( ctx.getChild(i).getText() );
		}

	}

	@Override public void exitStmt(@NotNull CParser.StmtContext ctx) {
		if( ctx.getParent() instanceof CParser.IfStmtContext ) {
			if( elseCheck == 1 ) {
				int i=0;
				intentSetting(1);
				for(;!(ctx.getParent().getChild(i).getText().contentEquals("else"));i++) {}
				for(; i<ctx.getParent().getChildCount();i++) {
					if( ctx.getParent().getChild(i) instanceof CParser.StmtContext )
						break;
					out.peek().append( ctx.getParent().getChild(i).getText() );
				}
				elseCheck--;
			}
		}
		if( ctx.getParent() instanceof CParser.WhileStmtContext ) {
			if( doWhileCheck == 1 ) {
				int i=0;
				intentSetting(1);
				for(;!(ctx.getParent().getChild(i).getText().contentEquals("while"));i++) {}
				for(; i<ctx.getParent().getChildCount() ; i++) {
					out.peek().append( ctx.getParent().getChild(i).getText() );
				}
				out.peek().append("\n");
				doWhileCheck--;
			}
		}
	}

	public void intentSetting(int abc) {
		for(int i=0; i<intentStair; i++)
			out.peek().append("	");
	}
}

public class C2CApp {
	public static void main(String[] args) throws IOException {
		// Input file		
		if (args.length != 1){
			System.err.println("Input C file");
			System.exit(1);
		}
		String filename = args[0];
		System.setIn(new FileInputStream(filename)); 
		InputStream input = System.in;

		// Get lexer
		CLexer lexer = new CLexer(new ANTLRInputStream(input));
		// Get a list of matched tokens
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// Pass tokens to parser
		CParser parser = new CParser(tokens);
		// Walk parse-tree and attach our listener
		ParseTreeWalker walker = new ParseTreeWalker();
		C2CListener listener = new C2CListener(parser);
		// walk from the root of parse tree
		walker.walk(listener, parser.program());
	
		// Output file
		FileOutputStream output = new FileOutputStream(new File("output_"+args[0]));
		System.out.println("Output file name:  output_"+args[0]);
		output.write(listener.out.peek().toString().getBytes());
		output.flush();
		output.close();
	}
} 
