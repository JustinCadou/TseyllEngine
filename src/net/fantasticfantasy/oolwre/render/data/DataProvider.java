/*Copyright (c) 2017 Fantastic Fantasy All rights reserved.
 *
 * Permission to use, copy, modify and/or redistribute in source or binary form
 * is hereby granted, free of charge, subject to the following conditions:
 *
 * - Redistribution of source code shall include the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form shall include the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * - Neither the name Object Oriented Lightweight Render Engine nor the names
 *  of its contributors may be used to endorse or promote products derived
 *  from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fantasticfantasy.oolwre.render.data;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.ARBSamplerObjects;
import org.lwjgl.opengl.ARBSeparateShaderObjects;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBTransformFeedback2;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import net.fantasticfantasy.oolwre.CapabilityProvider;
import net.fantasticfantasy.oolwre.render.data.shader.Shader;

/**The <code>DataProvider</code> class is used to allocate resources
 * using the correct extensions if the OpenGL versions required are not
 * supported by the computer.
 */
public class DataProvider {
	
	/** Thrown from methods that needs OpenGL 1.1 to be supported in order to be invoked. */
	private static final UnsupportedOperationException OPENGL_11;
	private static Map<Thread, DataProvider> threads;
	
	static {
		OPENGL_11 = new UnsupportedOperationException("OpenGL 1.1 must be supported in order to invoke this method!");
		threads = new HashMap<>();
	}
	
	private CapabilityProvider provider;
	
	private List<Integer> textures;
	private List<Integer> buffers;
	private List<Integer> queries;
	private List<Integer> programs;
	private List<Integer> shaders;
	private List<Integer> framebuffers;
	private List<Integer> renderbuffers;
	private List<Integer> vertexArrays;
	private List<Integer> samplers;
	private List<Integer> feedbacks;
	private List<Integer> pipelines;
	
	/**Constructs a {@link DataProvider} using the specified {@link CapabilityProvider}.
	 * 
	 * @param providerIn - The {@link CapabilityProvider} to check the capabilities
	 * 
	 * @throws NullPointerException If <code>providerIn</code> is <code>null</code>
	 */
	public DataProvider(CapabilityProvider providerIn) {
		if (providerIn == null) {
			throw new NullPointerException("Capability provider is null!");
		}
		this.provider = providerIn;
		this.textures = new ArrayList<>();
		this.buffers = new ArrayList<>();
		this.queries = new ArrayList<>();
		this.programs = new ArrayList<>();
		this.shaders = new ArrayList<>();
		this.framebuffers = new ArrayList<>();
		this.renderbuffers = new ArrayList<>();
		this.vertexArrays = new ArrayList<>();
		this.samplers = new ArrayList<>();
		this.feedbacks = new ArrayList<>();
		this.pipelines = new ArrayList<>();
	}
	
	/**Constructs a {@link DataProvider} using the current {@link Thread}'s
	 * {@link CapabilityProvider}. If there is no attached {@link CapabilityProvider},
	 * a {@link NullPointerException} is thrown.<br><br>
	 * {@link CapabilityProvider#get()} is used in order to retrieve it.
	 * 
	 * @throws NullPointerException If no {@link CapabilityProvider} is attached
	 * to the current {@link Thread}
	 */
	public DataProvider() {
		this(CapabilityProvider.get());
	}
	
	/**Links this {@link DataProvider} to the current {@link Thread} so
	 * that it can be used globally.
	 */
	public void link() {
		threads.put(Thread.currentThread(), this);
	}
	
	/**Deletes all the resources created using this {@link DataProvider}.
	 * The objects generated by methods starting by <code>s</code>, such as
	 * {@link #sgenTextures()}, are not included and must be deleted manually.
	 */
	public void deleteAll() {
		this.deleteTextures();
		this.deleteBuffers();
		this.deleteQueries();
		this.deleteShaders();
		this.deletePrograms();
		this.deleteFramebuffers();
		this.deleteRenderbuffers();
		this.deleteVertexArrays();
		this.deleteSamplers();
		this.deleteTransformFeedbacks();
		this.deleteProgramPipelines();
	}
	
	/**Deletes all the textures created by this {@link DataProvider}. The
	 * textures generated using {@link #sgenTextures()} are not included
	 * and must be manually deleted.
	 */
	public void deleteTextures() {
		for (int tex : this.textures) {
			this.sdeleteTextures(tex);
		}
		this.textures.clear();
	}
	
	/**Deletes all the buffers created by this {@link DataProvider}. The
	 * buffers generated using {@link #sgenBuffers()} are not included
	 * and must be manually deleted.
	 */
	public void deleteBuffers() {
		for (int buf : this.buffers) {
			this.sdeleteBuffers(buf);
		}
		this.buffers.clear();
	}
	
	/**Deletes all the queries created by this {@link DataProvider}. The
	 * queries generated using {@link #sgenQueries()} are not included
	 * and must be manually deleted.
	 */
	public void deleteQueries() {
		for (int query : this.queries) {
			this.sdeleteQueries(query);
		}
		this.queries.clear();
	}
	
	/**Deletes all the programs created by this {@link DataProvider}. The
	 * programs generated using {@link #sgenProgram()} are not included
	 * and must be manually deleted.
	 */
	public void deletePrograms() {
		for (int prog : this.programs) {
			this.sdeleteProgram(prog);
		}
		this.programs.clear();
	}
	
	/**Deletes all the shaders created by this {@link DataProvider}. The
	 * shaders generated using {@link #sgenShader(Shader.Type)} are not included
	 * and must be manually deleted.
	 */
	public void deleteShaders() {
		for (int shader : this.shaders) {
			this.sdeleteShader(shader);
		}
		this.shaders.clear();
	}
	
	/**Deletes all the framebuffers created by this {@link DataProvider}. The
	 * framebuffers generated using {@link #sgenFramebuffers()} are not included
	 * and must be manually deleted.
	 */
	public void deleteFramebuffers() {
		for (int frame : this.framebuffers) {
			this.sdeleteFramebuffers(frame);
		}
		this.framebuffers.clear();
	}
	
	/**Deletes all the renderbuffers created by this {@link DataProvider}. The
	 * renderbuffers generated using {@link #sgenRenderbuffers()} are not included
	 * and must be manually deleted.
	 */
	public void deleteRenderbuffers() {
		for (int render : this.renderbuffers) {
			this.sdeleteRenderbuffers(render);
		}
		this.renderbuffers.clear();
	}
	
	/**Deletes all the vertex arrays created by this {@link DataProvider}. The
	 * vertex arrays generated using {@link #sgenVertexArrays()} are not included
	 * and must be manually deleted.
	 */
	public void deleteVertexArrays() {
		for (int array : this.vertexArrays) {
			this.sdeleteVertexArrays(array);
		}
		this.vertexArrays.clear();
	}
	
	/**Deletes all the samplers created by this {@link DataProvider}. The
	 * samplers generated using {@link #sgenSamplers()} are not included
	 * and must be manually deleted.
	 */
	public void deleteSamplers() {
		for (int sampler : this.samplers) {
			this.sdeleteSamplers(sampler);
		}
		this.samplers.clear();
	}
	
	/**Deletes all the transform feedbacks created by this {@link DataProvider}.
	 * The transform feedbacks generated using {@link #sgenTransformFeedbacks()}
	 * are not included and must be manually deleted.
	 */
	public void deleteTransformFeedbacks() {
		for (int feedback : this.feedbacks) {
			this.sdeleteTransformFeedbacks(feedback);
		}
		this.feedbacks.clear();
	}
	
	/**Deletes all the program pipelines created by this {@link DataProvider}.
	 * The program pipelines generated using {@link #sgenProgramPipelines()} are
	 * not included and must be manually deleted.
	 */
	public void deleteProgramPipelines() {
		for (int pipeline : this.pipelines) {
			this.sdeleteProgramPipelines(pipeline);
		}
		this.pipelines.clear();
	}
	
	/**Returns <code>n</code> previously unused texture name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteTextures(int) deleteTextures(n)}.
	 */
	public int genTextures() {
		int tex = this.sgenTextures();
		this.textures.add(tex);
		return tex;
	}
	
	/**Puts <code>n</code> to <code>textures</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genTextures()}.
	 * 
	 * @param textures - Where to put <code>n</code>
	 */
	public void genTextures(IntBuffer textures) {
		textures.put(this.genTextures());
	}
	
	/**Deletes <code>n</code> used texture name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genTextures()}.
	 * If it was bound to any target, it is unbound from it. If it was attached
	 * to a framebuffer, special care must be taken.
	 * 
	 * @param textures - <code>n</code>
	 */
	public void deleteTextures(int textures) {
		if (this.textures.contains(textures)) {
			this.sdeleteTextures(textures);
			this.textures.remove((Object) textures);
		}
	}
	
	/**Returns <code>n</code> previously unused buffer name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteBuffers(int) deleteBuffers(n)}.
	 */
	public int genBuffers() {
		int buf = this.sgenBuffers();
		this.buffers.add(buf);
		return buf;
	}
	
	/**Puts <code>n</code> to <code>buffers</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genBuffers()}.
	 * 
	 * @param buffers - Where to put <code>n</code>
	 */
	public void genBuffers(IntBuffer buffers) {
		buffers.put(this.genBuffers());
	}
	
	/**Deletes <code>n</code> used buffer name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genBuffers()}.
	 * If it was bound to any target, it is unbound from it.
	 * 
	 * @param buffers - <code>n</code>
	 */
	public void deleteBuffers(int buffers) {
		if (this.buffers.contains(buffers)) {
			this.sdeleteBuffers(buffers);
			this.buffers.remove((Object) buffers);
		}
	}
	
	/**Returns <code>n</code> previously unused query name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteQueries(int) deleteQueries(n)}.
	 */
	public int genQueries() {
		int query = this.sgenQueries();
		this.queries.add(query);
		return query;
	}
	
	/**Puts <code>n</code> to <code>queries</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genQueries()}.
	 * 
	 * @param queries - Where to put <code>n</code>
	 */
	public void genQueries(IntBuffer queries) {
		queries.put(this.genQueries());
	}
	
	/**Deletes <code>n</code> used query name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genQueries()}.
	 * If it was querying to any target, it is interrupted.
	 * 
	 * @param queries - <code>n</code>
	 */
	public void deleteQueries(int queries) {
		if (this.queries.contains(queries)) {
			this.sdeleteQueries(queries);
			this.queries.remove((Object) queries);
		}
	}
	
	/**Returns <code>n</code> previously unused program name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteProgram(int) deleteProgram(n)}.
	 */
	public int genProgram() {
		int prog = this.sgenProgram();
		this.programs.add(prog);
		return prog;
	}
	
	/**Puts <code>n</code> to <code>programs</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genProgram()}.
	 * 
	 * @param programs - Where to put <code>n</code>
	 */
	public void genProgram(IntBuffer programs) {
		programs.put(this.genProgram());
	}
	
	/**Deletes <code>n</code> used program name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genProgram()}.
	 * If it was currently active, it is unactivated.
	 * 
	 * @param program - <code>n</code>
	 */
	public void deleteProgram(int program) {
		if (this.programs.contains(program)) {
			this.sdeleteProgram(program);
			this.programs.remove((Object) program);
		}
	}
	
	/**Returns <code>n</code> previously unused shader name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteShader(int) deleteShader(n)}.
	 * 
	 * @param type - The {@link Shader.Type}
	 */
	public int genShader(Shader.Type type) {
		int shader = this.sgenShader(type);
		this.shaders.add(shader);
		return shader;
	}
	
	/**Puts <code>n</code> to <code>shaders</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genShader(Shader.Type)}.
	 * 
	 * @param shaders - Where to put <code>n</code>
	 * @param type - The {@link Shader.Type}
	 */
	public void genShader(IntBuffer shaders, Shader.Type type) {
		shaders.put(this.genShader(type));
	}
	
	/**Deletes <code>n</code> used shader name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genShader(Shader.Type)}.
	 * If it was attached to a program, it is unattached from it.
	 * 
	 * @param shader - <code>n</code>
	 */
	public void deleteShader(int shader) {
		if (this.shaders.contains(shader)) {
			this.sdeleteShader(shader);
			this.shaders.remove((Object) shader);
		}
	}
	
	/**Returns <code>n</code> previously unused framebuffer name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteFramebuffers(int) deleteFramebuffers(n)}.
	 */
	public int genFramebuffers() {
		int frame = this.sgenFramebuffers();
		this.framebuffers.add(frame);
		return frame;
	}
	
	/**Puts <code>n</code> to <code>framebuffers</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genFramebuffers()}.
	 * 
	 * @param framebuffers - Where to put <code>n</code>
	 */
	public void genFramebuffers(IntBuffer framebuffers) {
		framebuffers.put(this.genFramebuffers());
	}
	
	/**Deletes <code>n</code> used framebuffer name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genFramebuffers()}.
	 * If it was bound to any target, it is unbound from it. If it was attached
	 * to a renderbuffer, special care must be taken.
	 * 
	 * @param framebuffers - <code>n</code>
	 */
	public void deleteFramebuffers(int framebuffers) {
		if (this.framebuffers.contains(framebuffers)) {
			this.sdeleteFramebuffers(framebuffers);
			this.framebuffers.remove((Object) framebuffers);
		}
	}
	
	/**Returns <code>n</code> previously unused renderbuffer name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteRenderbuffers(int) deleteRenderbuffers(n)}.
	 */
	public int genRenderbuffers() {
		int render = this.sgenRenderbuffers();
		this.renderbuffers.add(render);
		return render;
	}
	
	/**Puts <code>n</code> to <code>renderbuffers</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genRenderbuffers()}.
	 * 
	 * @param renderbuffers - Where to put <code>n</code>
	 */
	public void genRenderbuffers(IntBuffer renderbuffers) {
		renderbuffers.put(this.genRenderbuffers());
	}
	
	/**Deletes <code>n</code> used renderbuffer name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genRenderbuffers()}.
	 * If it was bound to any target, it is unbound from it. If it was attached
	 * to a framebuffer, special care must be taken.
	 * 
	 * @param renderbuffers - <code>n</code>
	 */
	public void deleteRenderbuffers(int renderbuffers) {
		if (this.renderbuffers.contains(renderbuffers)) {
			this.sdeleteRenderbuffers(renderbuffers);
			this.framebuffers.remove((Object) renderbuffers);
		}
	}
	
	/**Returns <code>n</code> previously unused vertex array name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteVertexArrays(int) deleteArrays(n)}.
	 */
	public int genVertexArrays() {
		int array = this.sgenVertexArrays();
		this.vertexArrays.add(array);
		return array;
	}
	
	/**Puts <code>n</code> to <code>arrays</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genVertexArrays()}.
	 * 
	 * @param arrays - Where to put <code>n</code>
	 */
	public void genVertexArrays(IntBuffer arrays) {
		arrays.put(this.genVertexArrays());
	}
	
	/**Deletes <code>n</code> used vertex array name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genVertexArrays()}.
	 * If it was bound, it is then unbound.
	 * 
	 * @param arrays - <code>n</code>
	 */
	public void deleteVertexArrays(int arrays) {
		if (this.vertexArrays.contains(arrays)) {
			this.sdeleteVertexArrays(arrays);
			this.vertexArrays.remove((Object) arrays);
		}
	}
	
	/**Returns <code>n</code> previously unused sampler name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteSamplers(int) deleteSamplers(n)}.
	 */
	public int genSamplers() {
		int sampler = this.genSamplers();
		this.samplers.add(sampler);
		return sampler;
	}
	
	/**Puts <code>n</code> to <code>samplers</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genSamplers()}.
	 * 
	 * @param samplers - Where to put <code>n</code>
	 */
	public void genSamplers(IntBuffer samplers) {
		samplers.put(this.genSamplers());
	}
	
	/**Deletes <code>n</code> used sampler name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genSamplers()}.
	 * If it was bound to any target, it is unbound from it.
	 * 
	 * @param samplers - <code>n</code>
	 */
	public void deleteSamplers(int samplers) {
		if (this.samplers.add(samplers)) {
			this.sdeleteSamplers(samplers);
			this.samplers.remove((Object) samplers);
		}
	}
	
	/**Returns <code>n</code> previously unused transform feedback name. <code>n</code>
	 * is then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteTransformFeedbacks(int) deleteTransformFeedbacks(n)}.
	 */
	public int genTransformFeedbacks() {
		int feedback = this.sgenTransformFeedbacks();
		this.feedbacks.add(feedback);
		return feedback;
	}
	
	/**Puts <code>n</code> to <code>feedbacks</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genTransformFeedbacks()}.
	 * 
	 * @param feedbacks - Where to put <code>n</code>
	 */
	public void genTransformFeedbacks(IntBuffer feedbacks) {
		feedbacks.put(this.genTransformFeedbacks());
	}
	
	/**Deletes <code>n</code> used transform feedback name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genTransformFeedbacks()}.
	 * 
	 * @param feedbacks - <code>n</code>
	 */
	public void deleteTransformFeedbacks(int feedbacks) {
		if (this.feedbacks.add(feedbacks)) {
			this.sdeleteTransformFeedbacks(feedbacks);
			this.feedbacks.remove((Object) feedbacks);
		}
	}
	
	/**Returns <code>n</code> previously unused program pipelines name. <code>n</code>
	 * is then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteProgramPipelines(int) deleteProgramPipelines(n)}.
	 */
	public int genProgramPipelines() {
		int pipeline = this.sgenProgramPipelines();
		this.pipelines.add(pipeline);
		return pipeline;
	}
	
	/**Puts <code>n</code> to <code>pipelines</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genProgramPipelines()}.
	 * 
	 * @param pipelines - Where to put <code>n</code>
	 */
	public void genProgramPipelines(IntBuffer pipelines) {
		pipelines.put(this.genProgramPipelines());
	}
	
	/**Deletes <code>n</code> used program pipeline name. <code>n</code> is marked
	 * <i>unused</i> and may be returned once again by {@link #genProgramPipelines()}.
	 * If it was bound, it is then unbound.
	 * 
	 * @param textures - <code>n</code>
	 */
	public void deleteProgramPipelines(int pipelines) {
		if (this.pipelines.contains(pipelines)) {
			this.sdeleteProgramPipelines(pipelines);
			this.pipelines.remove((Object) pipelines);
		}
	}
	
	/**Returns <code>n</code> previously unused texture name. <code>n</code> is
	 * then marked <i>used</i> and wont be returned until the next call of
	 * {@link #deleteProgram(int) deleteProgram(n)}.
	 * 
	 * @param type - The {@link Shader.Type}
	 * @param strings - The shader source
	 */
	public int genShaderProgramv(Shader.Type type, CharSequence... strings) {
		int prog = this.sgenShaderProgramv(type, strings);
		this.programs.add(prog);
		return prog;
	}
	
	/**Puts <code>n</code> to <code>programs</code>, where <code><i>n</i></code> is
	 * the returned value by {@link #genShaderProgramv()}.
	 * 
	 * @param programs - Where to put <code>n</code>
	 * @param type - The {@link Shader.Type}
	 * @param strings - The pointer to the shader program source code
	 */
	public void genShaderProgramv(IntBuffer programs, Shader.Type type, CharSequence... strings) {
		programs.put(this.genShaderProgramv(type, strings));
	}
	
	/**Unrecommended version of {@link #genTextures()}.
	 */
	public int sgenTextures() {
		if (this.provider.isOpenGLVersionSupported(11)) {
			return GL11.glGenTextures();
		} else {
			throw OPENGL_11;
		}
	}
	
	/**Unrecommended version of {@link #deleteTextures(int)}.
	 */
	public void sdeleteTextures(int textures) {
		if (this.provider.isOpenGLVersionSupported(11)) {
			GL11.glDeleteTextures(textures);
		} else {
			throw OPENGL_11;
		}
	}
	
	/**Unrecommended version of {@link #genBuffers()}.
	 */
	public int sgenBuffers() {
		if (this.provider.isOpenGLVersionSupported(15)) {
			return GL15.glGenBuffers();
		} else {
			return ARBVertexBufferObject.glGenBuffersARB();
		}
	}
	
	/**Unrecommended version of {@link #deleteBuffers(int)}.
	 */
	public void sdeleteBuffers(int buffers) {
		if (this.provider.isOpenGLVersionSupported(15)) {
			GL15.glDeleteBuffers(buffers);
		} else {
			ARBVertexBufferObject.glDeleteBuffersARB(buffers);
		}
	}
	
	/**Unrecommended version of {@link #genQueries()}.
	 */
	public int sgenQueries() {
		if (this.provider.isOpenGLVersionSupported(15)) {
			return GL15.glGenQueries();
		} else {
			return ARBOcclusionQuery.glGenQueriesARB();
		}
	}
	
	/**Unrecommended version of {@link #deleteQueries(int)}.
	 */
	public void sdeleteQueries(int queries) {
		if (this.provider.isOpenGLVersionSupported(15)) {
			GL15.glDeleteQueries(queries);
		} else {
			ARBOcclusionQuery.glDeleteQueriesARB(queries);
		}
	}
	
	/**Unrecommended version of {@link #genProgram()}.
	 */
	public int sgenProgram() {
		if (this.provider.isOpenGLVersionSupported(20)) {
			return GL20.glCreateProgram();
		} else {
			return ARBShaderObjects.glCreateProgramObjectARB();
		}
	}
	
	/**Unrecommended version of {@link #deleteProgram(int)}.
	 */
	public void sdeleteProgram(int program) {
		if (this.provider.isOpenGLVersionSupported(20)) {
			GL20.glDeleteProgram(program);
		} else {
			ARBShaderObjects.glDeleteObjectARB(program);
		}
	}
	
	/**Unrecommended version of {@link #genShader(Shader.Type)}.
	 */
	public int sgenShader(Shader.Type type) {
		if (this.provider.isOpenGLVersionSupported(20)) {
			return GL20.glCreateShader(type.glValue());
		} else {
			return ARBShaderObjects.glCreateShaderObjectARB(type.glValue());
		}
	}
	
	/**Unrecommended version of {@link #deleteShader(int)}.
	 */
	public void sdeleteShader(int shader) {
		if (this.provider.isOpenGLVersionSupported(20)) {
			GL20.glDeleteShader(shader);
		} else {
			ARBShaderObjects.glDeleteObjectARB(shader);
		}
	}
	
	/**Unrecommended version of {@link #genFramebuffers()}.
	 */
	public int sgenFramebuffers() {
		if (this.provider.isOpenGLVersionSupported(30)) {
			return GL30.glGenFramebuffers();
		} else {
			return ARBFramebufferObject.glGenFramebuffers();
		}
	}
	
	/**Unrecommended version of {@link #deleteFramebuffers(int)}.
	 */
	public void sdeleteFramebuffers(int framebuffers) {
		if (this.provider.isOpenGLVersionSupported(30)) {
			GL30.glDeleteFramebuffers(framebuffers);
		} else {
			ARBFramebufferObject.glDeleteFramebuffers(framebuffers);
		}
	}
	
	/**Unrecommended version of {@link #genRenderbuffers()}.
	 */
	public int sgenRenderbuffers() {
		if (this.provider.isOpenGLVersionSupported(30)) {
			return GL30.glGenRenderbuffers();
		} else {
			return ARBFramebufferObject.glGenRenderbuffers();
		}
	}
	
	/**Unrecommended version of {@link #deleteRenderbuffers(int)}.
	 */
	public void sdeleteRenderbuffers(int renderbuffers) {
		if (this.provider.isOpenGLVersionSupported(30)) {
			GL30.glDeleteRenderbuffers(renderbuffers);
		} else {
			ARBFramebufferObject.glDeleteRenderbuffers(renderbuffers);
		}
	}
	
	/**Unrecommended version of {@link #genVertexArrays()}.
	 */
	public int sgenVertexArrays() {
		if (this.provider.isOpenGLVersionSupported(30)) {
			return GL30.glGenVertexArrays();
		} else {
			return ARBVertexArrayObject.glGenVertexArrays();
		}
	}
	
	/**Unrecommended version of {@link #deleteVertexArrays(int)}.
	 */
	public void sdeleteVertexArrays(int arrays) {
		if (this.provider.isOpenGLVersionSupported(30)) {
			GL30.glDeleteVertexArrays(arrays);
		} else {
			ARBVertexArrayObject.glDeleteVertexArrays(arrays);
		}
	}
	
	/**Unrecommended version of {@link #genSamplers()}.
	 */
	public int sgenSamplers() {
		if (this.provider.isOpenGLVersionSupported(33)) {
			return GL33.glGenSamplers();
		} else {
			return ARBSamplerObjects.glGenSamplers();
		}
	}
	
	/**Unrecommended version of {@link #deleteSamplers(int)}.
	 */
	public void sdeleteSamplers(int samplers) {
		if (this.provider.isOpenGLVersionSupported(33)) {
			GL33.glDeleteSamplers(samplers);
		} else {
			ARBSamplerObjects.glDeleteSamplers(samplers);
		}
	}
	
	/**Unrecommended version of {@link #genTransformFeedbacks()}.
	 */
	public int sgenTransformFeedbacks() {
		if (this.provider.isOpenGLVersionSupported(40)) {
			return GL40.glGenTransformFeedbacks();
		} else {
			return ARBTransformFeedback2.glGenTransformFeedbacks();
		}
	}
	
	/**Unrecommended version of {@link #deleteTransformFeedbacks(int)}.
	 */
	public void sdeleteTransformFeedbacks(int feedbacks) {
		if (this.provider.isOpenGLVersionSupported(40)) {
			GL40.glDeleteTransformFeedbacks(feedbacks);
		} else {
			ARBTransformFeedback2.glDeleteTransformFeedbacks(feedbacks);
		}
	}
	
	/**Unrecommended version of {@link #genProgramPipelines()}.
	 */
	public int sgenProgramPipelines() {
		if (this.provider.isOpenGLVersionSupported(41)) {
			return GL41.glGenProgramPipelines();
		} else {
			return ARBSeparateShaderObjects.glGenProgramPipelines();
		}
	}
	
	/**Unrecommended version of {@link #deleteProgramPipelines(int)}.
	 */
	public void sdeleteProgramPipelines(int programs) {
		if (this.provider.isOpenGLVersionSupported(41)) {
			GL41.glDeleteProgramPipelines(programs);
		} else {
			ARBSeparateShaderObjects.glDeleteProgramPipelines(programs);
	
		}
	}
	
	/**Unrecommended version of {@link #genShaderProgramv(Shader.Type, CharSequence...)}.
	 */
	public int sgenShaderProgramv(Shader.Type type, CharSequence... strings) {
		if (this.provider.isOpenGLVersionSupported(41)) {
			return GL41.glCreateShaderProgramv(type.glValue(), strings);
		} else {
			return ARBSeparateShaderObjects.glCreateShaderProgramv(type.glValue(), strings);
		}
	}
	
	/**Retrieves the current {@link Thread}'s {@link DataProvider}, or <code>null</code>
	 * if none was attached.
	 * 
	 * @return The {@link DataProvider} attached to the current {@link Thread}
	 */
	public static DataProvider get() {
		return threads.get(Thread.currentThread());
	}
}
