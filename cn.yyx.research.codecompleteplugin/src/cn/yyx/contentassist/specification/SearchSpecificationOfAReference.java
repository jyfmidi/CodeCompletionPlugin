package cn.yyx.contentassist.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AnonymousTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.FillArgumentNamesCompletionProposalCollector;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuessingProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import cn.yyx.contentassist.codecompletion.CodeCompletionMetaInfo;
import cn.yyx.contentassist.codecompletion.PredictMetaInfo;
import cn.yyx.contentassist.commonutils.ClassInstanceOfUtil;
import cn.yyx.contentassist.commonutils.SimilarityHelper;
import cn.yyx.contentassist.commonutils.TimeOutProgressMonitor;
import cn.yyx.contentassist.commonutils.YJCache;

@SuppressWarnings("restriction")
public class SearchSpecificationOfAReference {
	// ambugious
	/*
	 * public static List<TypeMember> SearchTypeSpecificationByPrefix(String
	 * prefix, JavaContentAssistInvocationContext javacontext, boolean
	 * ambiguous) { List<TypeMember> tmlist = new LinkedList<TypeMember>();
	 * CompletionProposalCollector collector =
	 * GetTypeMemberProposalCollector(javacontext); TimeOutProgressMonitor topm
	 * = new TimeOutProgressMonitor(CodeCompletionMetaInfo.typetimeout);
	 * List<ICompletionProposal> proposals =
	 * SearchSpecificationByPrefix(collector, prefix, javacontext, topm);
	 * Iterator<ICompletionProposal> itr = proposals.iterator();
	 * Queue<MemberSorter> prioriqueue = new PriorityQueue<MemberSorter>();
	 * while (itr.hasNext()) { ICompletionProposal icp = itr.next();
	 * LazyGenericTypeProposal lgtp = (LazyGenericTypeProposal) icp; String
	 * display = lgtp.getDisplayString(); if (CodeCompletionMetaInfo.DebugMode)
	 * { System.err.println(display); } String[] dps = display.split("-");
	 * String rt = dps[0].trim(); String pkg = dps[1].trim(); String tp = pkg +
	 * "." + rt; Class<?> tpclass = null; try { tpclass = Class.forName(tp); }
	 * catch (ClassNotFoundException e) { System.err.println("Unresolved class:"
	 * + tp); continue; // e.printStackTrace(); } double similarity =
	 * SimilarityHelper.ComputeTwoStringSimilarity(prefix, rt); TypeMember tm =
	 * new TypeMember(tp, tpclass); prioriqueue.add(new MemberSorter(similarity,
	 * tm)); }
	 * 
	 * int total = 0; while (!(prioriqueue.isEmpty())) { MemberSorter ms =
	 * prioriqueue.poll(); total++; if (total >
	 * PredictMetaInfo.MaxTypeSpecificationSize || (total > 0 &&
	 * ms.getSimilarity() <= PredictMetaInfo.TwoTypeStringSimilarThreshold)) {
	 * break; } tmlist.add(0, (TypeMember) ms.getMember()); } return tmlist; }
	 */

	/*
	 * private static String GetPrefixCmp(String spechint) { int idx =
	 * spechint.lastIndexOf('.'); if (idx < 0) { idx =
	 * spechint.lastIndexOf(':'); } if (idx == spechint.length()-1) { return
	 * null; } return spechint.substring(idx+1); }
	 */
	
	private static YJCache<List<FieldMember>> fieldcache = new YJCache<List<FieldMember>>();
	private static YJCache<List<TypeMember>> typecache = new YJCache<List<TypeMember>>();
	private static YJCache<List<MethodMember>> methodcache = new YJCache<List<MethodMember>>();
	
	private static String DebugExtraInfo = "";
	
	public static void Reset()
	{
		fieldcache.Clear();
		// typecache.Clear();
		methodcache.Clear();
	}

	public static List<FieldMember> SearchFieldSpecificationByPrefix(String prefix,
			JavaContentAssistInvocationContext javacontext) {
		
		// get cache.
		List<FieldMember> fc = fieldcache.GetCachedContent(prefix);
		if (fc != null)
		{
			return fc;
		}
		
		String prefixcmp = SpecificationHelper.GetPrefixCmp(prefix);
		CompletionProposalCollector collector = GetFieldMemberProposalCollector(javacontext);
		TimeOutProgressMonitor topm = new TimeOutProgressMonitor(CodeCompletionMetaInfo.fieldtimeout);
		List<ICompletionProposal> proposals = SearchSpecificationByPrefix(collector, prefix, javacontext, topm);
		Iterator<ICompletionProposal> itr = proposals.iterator();
		Queue<MemberSorter> prioriqueue = new PriorityQueue<MemberSorter>();
		while (itr.hasNext()) {
			ICompletionProposal icp = itr.next();
			JavaCompletionProposal jcp = (JavaCompletionProposal) icp;
			String pstr = jcp.getDisplayString().trim();
			if (CodeCompletionMetaInfo.DebugMode) {
				System.err.println(DebugExtraInfo + pstr);
			}
			String[] strs = pstr.split(":|-");
			String fieldname = strs[0].trim();
			String fieldtype = strs[1].trim();
			String wheredeclared = null;
			if (strs.length == 3) {
				wheredeclared = strs[2].trim();
			}
			double similarity = 1;
			if (prefixcmp != null) {
				similarity = SimilarityHelper.ComputeTwoStringSimilarity(prefixcmp, fieldname);
			}
			FieldMember fm = new FieldMember(fieldname, fieldtype, wheredeclared);
			prioriqueue.add(new MemberSorter(similarity, pstr, fm));
		}

		List<FieldMember> fmlist = new LinkedList<FieldMember>();
		int total = 0;
		while (!(prioriqueue.isEmpty())) {
			MemberSorter ms = prioriqueue.poll();
			total++;
			if (total > PredictMetaInfo.MaxFieldSpecificationSize
					|| (total > 0 && ms.getSimilarity() <= PredictMetaInfo.TwoFieldStringSimilarThreshold)) {
				break;
			}
			fmlist.add((FieldMember) ms.getMember());
		}
		fieldcache.AddCachePair(prefix, fmlist);
		return fmlist;
	}

	public static List<TypeMember> SearchFieldClassMemberSpecificationByPrefix(String prefix,
			JavaContentAssistInvocationContext javacontext) {
		
		if (!prefix.endsWith(".class"))
		{
			if (prefix.endsWith(".")) {
				prefix = prefix + "class";
			} else {
				prefix = prefix + ".class";
			}
		}
		// get cache
		List<TypeMember> tc = typecache.GetCachedContent(prefix);
		if (tc != null)
		{
			return tc;
		}
		
		CompletionProposalCollector collector = GetFieldClassMemberProposalCollector(javacontext);
		TimeOutProgressMonitor topm = new TimeOutProgressMonitor(CodeCompletionMetaInfo.typetimeout);
		List<ICompletionProposal> proposals = SearchSpecificationByPrefix(collector, prefix, javacontext, // prefix + ".class"
				topm);
		Iterator<ICompletionProposal> itr = proposals.iterator();
		List<TypeMember> tmlist = new LinkedList<TypeMember>();
		while (itr.hasNext()) {
			ICompletionProposal icp = itr.next();
			JavaCompletionProposal jcp = (JavaCompletionProposal) icp;
			String pstr = jcp.getDisplayString().trim();
			if (!pstr.startsWith("class ")) {
				continue;
			}
			if (CodeCompletionMetaInfo.DebugMode) {
				System.err.println(DebugExtraInfo + pstr);
				// System.err.println(icp.getClass());
			}
			int classbegin = pstr.indexOf('<');
			int classend = pstr.lastIndexOf('>');
			String classfullname = pstr.substring(classbegin + 1, classend);
			Class<?> cls = null;
			try {
				// cls = Class.forName(classfullname);
				cls = OmnipotentClassLoader.LoadClass(classfullname);
			} catch (Exception e) {
				e.printStackTrace();
			}
			TypeMember tm = new TypeMember(classfullname, cls);
			tmlist.add(tm);
		}
		typecache.AddCachePair(prefix, tmlist);
		return tmlist;
	}

	/*
	 * private static boolean IsEndWithNoConcreate(String scnt) { int idx =
	 * scnt.lastIndexOf('.'); if (idx < 0) { idx = scnt.lastIndexOf(':'); } if
	 * (scnt.length() > 0 && idx == scnt.length()-1) { return true; } return
	 * false; }
	 */

	public static List<MethodMember> SearchMethodSpecificationByPrefix(String prefix,
			JavaContentAssistInvocationContext javacontext) {

		// get cache
		List<MethodMember> mc = methodcache.GetCachedContent(prefix);
		if (mc != null)
		{
			return mc;
		}
		
		prefix = prefix.trim();
		boolean methodref = false;
		if (prefix.endsWith("::")) {
			methodref = true;
		}
		String prefixcmp = SpecificationHelper.GetPrefixCmp(prefix);
		/*
		 * if (prefix.startsWith("new ") || prefix.contains(".new ")) {
		 * prefixcmp = StringUtil.GetContentBehindFirstWhiteSpace(prefix); }
		 * else { prefixcmp = GetPrefixCmp(prefix); }
		 */
		CompletionProposalCollector collector = GetMethodMemberProposalCollector(javacontext);
		TimeOutProgressMonitor topm = new TimeOutProgressMonitor(CodeCompletionMetaInfo.methodtimeout);
		List<ICompletionProposal> proposals = SearchSpecificationByPrefix(collector, prefix, javacontext, topm);
		Iterator<ICompletionProposal> itr = proposals.iterator();
		Queue<MemberSorter> prioriqueue = new PriorityQueue<MemberSorter>();
		while (itr.hasNext()) {
			ICompletionProposal icp = itr.next();
			String pstr = null;
			boolean anonymous = false;
			if (ClassInstanceOfUtil.ObjectInstanceOf(icp, AnonymousTypeCompletionProposal.class)) {
				AnonymousTypeCompletionProposal atcp = (AnonymousTypeCompletionProposal) icp;
				pstr = atcp.getDisplayString();
				anonymous = true;
				if (CodeCompletionMetaInfo.DebugMode)
				{
					System.err.println(DebugExtraInfo + atcp.getReplacementString());
					System.err.println(DebugExtraInfo + atcp.getSortString());
					System.err.println(DebugExtraInfo + atcp.getReplacementOffset());
					System.err.println(DebugExtraInfo + atcp.getReplacementLength());
				}
			}
			if (ClassInstanceOfUtil.ObjectInstanceOf(icp, JavaMethodCompletionProposal.class)) {
				JavaMethodCompletionProposal jmip = (JavaMethodCompletionProposal) icp;
				pstr = jmip.getDisplayString();
			}
			if (ClassInstanceOfUtil.ObjectInstanceOf(icp, ParameterGuessingProposal.class)) {
				ParameterGuessingProposal jmip = (ParameterGuessingProposal) icp;
				pstr = jmip.getDisplayString();
			}
			
			if (CodeCompletionMetaInfo.DebugMode) {
				System.err.println(DebugExtraInfo + pstr);
			}

			if (pstr != null) {
				String funcname = null;
				LinkedList<String> argtypelist = new LinkedList<String>();
				LinkedList<String> argnamelist = new LinkedList<String>();
				String returntype = null;
				String wheredeclared = null;
				if (anonymous) {
					int idx = pstr.indexOf("Anonymous Inner Type");
					if (idx < 0)
					{
						System.err.println("Not Anonymous Inner Type? A new Type? Unkown type is:" + pstr);
						System.exit(1);
					}
					String function = pstr.substring(0, idx).trim();
					String[] funs = function.split("\\(|\\)|(, )");
					funcname = (funs[0].trim());
					int flen = funs.length;
					for (int i = 1; i < flen; i++) {
						String arg = funs[i].trim();
						argtypelist.add(arg);
						argnamelist.add("@Unknown");
					}
					String packinfo = pstr.substring(idx);
					String[] pis = packinfo.split("-");
					returntype = pis[1].trim() + "." + funcname;
				} else {
					if (methodref) {
						String[] strs = pstr.split(":|-");
						String[] funs = strs[0].trim().split("\\(|\\)|(, )");
						funcname = (funs[0].trim());
						int flen = funs.length;
						for (int i = 1; i < flen; i++) {
							String arg = funs[i].trim();
							argtypelist.add(arg);
							argnamelist.add("@Unknown");
						}
						if (strs.length >= 2) {
							returntype = strs[1].trim();
							wheredeclared = strs[2].trim();
						}
					} else {
						String[] strs = pstr.split(":|-");
						String[] funs = strs[0].trim().split("\\(|\\)|(, )");
						funcname = (funs[0].trim());
						int flen = funs.length;
						for (int i = 1; i < flen; i++) {
							String arg = funs[i].trim();
							int wsidx = arg.lastIndexOf(' ');
							argtypelist.add(arg.substring(0, wsidx));
							argnamelist.add(arg.substring(wsidx + 1));
						}
						returntype = (strs[1].trim());
						wheredeclared = null;
						if (strs.length == 3) {
							wheredeclared = strs[2].trim();
						}
					}
				}
				double similarity = 1;
				if (prefixcmp != null) {
					similarity = SimilarityHelper.ComputeTwoStringSimilarity(prefixcmp, funcname);
				}
				MethodMember mm = new MethodMember(funcname, returntype, wheredeclared, argnamelist, argtypelist);
				prioriqueue.add(new MemberSorter(similarity, pstr, mm));
			}
		}

		List<MethodMember> mmlist = new LinkedList<MethodMember>();
		int total = 0;
		while (!(prioriqueue.isEmpty())) {
			MemberSorter ms = prioriqueue.poll();
			total++;
			if (total > PredictMetaInfo.MaxMethodSpecificationSize
					|| (total > 0 && ms.getSimilarity() <= PredictMetaInfo.TwoMethodStringSimilarityThreshold)) {
				break;
			}
			mmlist.add((MethodMember) ms.getMember());
		}
		methodcache.AddCachePair(prefix, mmlist);
		return mmlist;
	}

	public static MembersOfAReference SearchFunctionSpecificationByPrefix(String prefix,
			JavaContentAssistInvocationContext javacontext) {
		// the prefix must be as the following form: <form:System.out.>
		MembersOfAReference result = new MembersOfAReference();
		CompletionProposalCollector collector = GetProposalCollector(javacontext);
		TimeOutProgressMonitor topm = new TimeOutProgressMonitor(CodeCompletionMetaInfo.alltimeout);
		List<ICompletionProposal> proposals = SearchSpecificationByPrefix(collector, prefix, javacontext, topm);
		// System.out.println("start print proposals. proposals length:" +
		// proposals.size());
		Iterator<ICompletionProposal> itr = proposals.iterator();
		int idx = 0;
		while (itr.hasNext()) {
			idx++;
			ICompletionProposal icp = itr.next();
			// interested
			System.err.println(DebugExtraInfo + "proposal" + idx + " display : " + icp.getDisplayString());
			System.err.println(DebugExtraInfo + "proposal" + idx + " type : " + icp.getClass());
			System.err.println(DebugExtraInfo + "proposal" + idx + " : " + icp.toString());
			System.err.println("========================");
			if (ClassInstanceOfUtil.ObjectInstanceOf(icp, AnonymousTypeCompletionProposal.class)) {
				AnonymousTypeCompletionProposal atcp = (AnonymousTypeCompletionProposal) icp;
				if (CodeCompletionMetaInfo.DebugMode)
				{
					System.err.println(DebugExtraInfo + atcp.getReplacementString());
					System.err.println(DebugExtraInfo + atcp.getSortString());
					System.err.println(DebugExtraInfo + atcp.getReplacementOffset());
					System.err.println(DebugExtraInfo + atcp.getReplacementLength());
				}
			}
		}
		// testing
		// System.out.println(result);
		return result;
	}

	private static List<ICompletionProposal> SearchSpecificationByPrefix(CompletionProposalCollector collector,
			String prefix, JavaContentAssistInvocationContext javacontext, IProgressMonitor monitor) {
		int rawoffset = javacontext.getInvocationOffset();
		int position = rawoffset + prefix.length();

		DefaultWorkingCopyOwner owner = DefaultWorkingCopyOwner.PRIMARY;

		try {
			ICompilationUnit sourceunit = javacontext.getCompilationUnit();
			MyCompilationUnit mcu = new MyCompilationUnit(
					(org.eclipse.jdt.internal.compiler.env.ICompilationUnit) sourceunit, javacontext.getDocument(),
					prefix, rawoffset);
			JavaProject project = (JavaProject) sourceunit.getJavaProject();
			SearchableEnvironment environment = project.newSearchableNameEnvironment(owner);
			// code complete
			CompletionEngine engine = new CompletionEngine(environment, collector, project.getOptions(true), project,
					owner, monitor);
			engine.complete(mcu, position, 0, sourceunit);
		} catch (Exception x) {
			x.printStackTrace();
		}

		ICompletionProposal[] javaProposals = collector.getJavaCompletionProposals();

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>(Arrays.asList(javaProposals));
		// if (proposals.size() == 0) {
		// String error= collector.getErrorMessage();
		// if (error.length() > 0)
		// new Exception().printStackTrace();
		// }
		return proposals;
	}

	private static CompletionProposalCollector GetProposalCollector(JavaContentAssistInvocationContext javacontext) {
		CompletionProposalCollector collector = null;
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)) {
			collector = new FillArgumentNamesCompletionProposalCollector(javacontext);
		} else {
			collector = new CompletionProposalCollector(javacontext.getCompilationUnit(), true);
		}
		collector.setInvocationContext(javacontext);
		collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, false);
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, false);
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
		collector.setIgnored(CompletionProposal.FIELD_REF, false);
		collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, false);
		collector.setIgnored(CompletionProposal.KEYWORD, false);
		collector.setIgnored(CompletionProposal.LABEL_REF, false);
		collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
		collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
		collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, false);
		collector.setIgnored(CompletionProposal.METHOD_REF, false);
		collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, false);
		collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, false);
		collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
		collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, false);
		collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
		collector.setIgnored(CompletionProposal.TYPE_REF, false);

		// Allow completions for unresolved types - since 3.3
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF,
				true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
				CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
				CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
		// Set the favorite list to propose static members - since 3.3
		collector.setFavoriteReferences(getFavoriteStaticMembers());

		return collector;
	}

	private static String[] getFavoriteStaticMembers() {
		String serializedFavorites = PreferenceConstants.getPreferenceStore()
				.getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
		if (serializedFavorites != null && serializedFavorites.length() > 0)
			return serializedFavorites.split(";"); //$NON-NLS-1$
		return new String[0];
	}

	/*
	 * private static CompletionProposalCollector
	 * GetTypeMemberProposalCollector( JavaContentAssistInvocationContext
	 * javacontext) { CompletionProposalCollector collector = null; if
	 * (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.
	 * CODEASSIST_FILL_ARGUMENT_NAMES)) { collector = new
	 * FillArgumentNamesCompletionProposalCollector(javacontext); } else {
	 * collector = new
	 * CompletionProposalCollector(javacontext.getCompilationUnit(), true); }
	 * collector.setInvocationContext(javacontext); //
	 * collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, //
	 * false); //
	 * collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, //
	 * false); // collector.setIgnored(CompletionProposal.
	 * ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, // false); //
	 * collector.setIgnored(CompletionProposal.FIELD_REF, false); //
	 * collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER,
	 * // false); // collector.setIgnored(CompletionProposal.KEYWORD, false); //
	 * collector.setIgnored(CompletionProposal.LABEL_REF, false); //
	 * collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false); //
	 * collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false); //
	 * collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, // false);
	 * // collector.setIgnored(CompletionProposal.METHOD_REF, false); //
	 * collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, //
	 * false); //
	 * collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER,
	 * // false); // collector.setIgnored(CompletionProposal.PACKAGE_REF,
	 * false); //
	 * collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, //
	 * false); // collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION,
	 * false); collector.setIgnored(CompletionProposal.TYPE_REF, false);
	 * 
	 * // Allow completions for unresolved types - since 3.3 //
	 * collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, //
	 * CompletionProposal.TYPE_REF, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, //
	 * CompletionProposal.TYPE_IMPORT, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, //
	 * CompletionProposal.FIELD_IMPORT, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, //
	 * CompletionProposal.TYPE_REF, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, //
	 * CompletionProposal.TYPE_IMPORT, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, //
	 * CompletionProposal.METHOD_IMPORT, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.
	 * CONSTRUCTOR_INVOCATION, // CompletionProposal.TYPE_REF, true); //
	 * collector.setAllowsRequiredProposals(CompletionProposal.
	 * ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, // CompletionProposal.TYPE_REF,
	 * true); // collector.setAllowsRequiredProposals(CompletionProposal.
	 * ANONYMOUS_CLASS_DECLARATION, // CompletionProposal.TYPE_REF, true);
	 * collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF,
	 * CompletionProposal.TYPE_REF, true);
	 * 
	 * return collector; }
	 */

	private static CompletionProposalCollector GetFieldMemberProposalCollector(
			JavaContentAssistInvocationContext javacontext) {
		CompletionProposalCollector collector = null;
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)) {
			collector = new FillArgumentNamesCompletionProposalCollector(javacontext);
		} else {
			collector = new CompletionProposalCollector(javacontext.getCompilationUnit(), true);
		}
		collector.setInvocationContext(javacontext);
		// collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF,
		// false);
		// collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
		// false);
		// collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
		// false);
		collector.setIgnored(CompletionProposal.FIELD_REF, false);
		collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER, false);
		// collector.setIgnored(CompletionProposal.KEYWORD, false);
		// collector.setIgnored(CompletionProposal.LABEL_REF, false);
		// collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
		// collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE,
		// false);
		// collector.setIgnored(CompletionProposal.METHOD_REF, false);
		// collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION,
		// false);
		// collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER,
		// false);
		// collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
		// collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION,
		// false);
		// collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.TYPE_REF, false);

		// Allow completions for unresolved types - since 3.3
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.TYPE_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.METHOD_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF,
		// CompletionProposal.TYPE_REF, true);

		return collector;
	}

	private static CompletionProposalCollector GetFieldClassMemberProposalCollector(
			JavaContentAssistInvocationContext javacontext) {
		CompletionProposalCollector collector = null;
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)) {
			collector = new FillArgumentNamesCompletionProposalCollector(javacontext);
		} else {
			collector = new CompletionProposalCollector(javacontext.getCompilationUnit(), true);
		}
		collector.setInvocationContext(javacontext);
		// collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF,
		// false);
		// collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
		// false);
		// collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
		// false);
		collector.setIgnored(CompletionProposal.FIELD_REF, false);
		// collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER,
		// false);
		// collector.setIgnored(CompletionProposal.KEYWORD, false);
		// collector.setIgnored(CompletionProposal.LABEL_REF, false);
		// collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
		// collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE,
		// false);
		// collector.setIgnored(CompletionProposal.METHOD_REF, false);
		// collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION,
		// false);
		// collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER,
		// false);
		// collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
		// collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION,
		// false);
		// collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.TYPE_REF, false);

		// Allow completions for unresolved types - since 3.3
		collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF,
		// CompletionProposal.TYPE_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF,
		// CompletionProposal.FIELD_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.TYPE_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF,
		// CompletionProposal.METHOD_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF,
		// CompletionProposal.TYPE_REF, true);

		return collector;
	}

	private static CompletionProposalCollector GetMethodMemberProposalCollector(
			JavaContentAssistInvocationContext javacontext) {
		CompletionProposalCollector collector = null;
		if (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES)) {
			collector = new FillArgumentNamesCompletionProposalCollector(javacontext);
		} else {
			collector = new CompletionProposalCollector(javacontext.getCompilationUnit(), true);
		}
		collector.setInvocationContext(javacontext);
		// collector.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF,
		// false);
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_DECLARATION, false);
		collector.setIgnored(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION, false);
		// collector.setIgnored(CompletionProposal.FIELD_REF, false);
		// collector.setIgnored(CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER,
		// false);
		// collector.setIgnored(CompletionProposal.KEYWORD, false);
		// collector.setIgnored(CompletionProposal.LABEL_REF, false);
		// collector.setIgnored(CompletionProposal.LOCAL_VARIABLE_REF, false);
		collector.setIgnored(CompletionProposal.METHOD_DECLARATION, false);
		collector.setIgnored(CompletionProposal.METHOD_NAME_REFERENCE, false);
		collector.setIgnored(CompletionProposal.METHOD_REF, false);
		collector.setIgnored(CompletionProposal.CONSTRUCTOR_INVOCATION, false);
		collector.setIgnored(CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER, false);
		// collector.setIgnored(CompletionProposal.PACKAGE_REF, false);
		collector.setIgnored(CompletionProposal.POTENTIAL_METHOD_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.VARIABLE_DECLARATION, false);
		// collector.setIgnored(CompletionProposal.TYPE_REF, false);

		// Allow completions for unresolved types - since 3.3
		// collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF,
		// CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF,
		// CompletionProposal.TYPE_IMPORT, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.FIELD_REF,
		// CompletionProposal.FIELD_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);
		collector.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF,
				true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
				CompletionProposal.TYPE_REF, true);
		collector.setAllowsRequiredProposals(CompletionProposal.ANONYMOUS_CLASS_DECLARATION,
				CompletionProposal.TYPE_REF, true);
		// collector.setAllowsRequiredProposals(CompletionProposal.TYPE_REF,
		// CompletionProposal.TYPE_REF, true);

		return collector;
	}

	public static String getDebugExtraInfo() {
		return DebugExtraInfo;
	}

	public static void setDebugExtraInfo(String debugExtraInfo) {
		DebugExtraInfo = debugExtraInfo;
	}

}