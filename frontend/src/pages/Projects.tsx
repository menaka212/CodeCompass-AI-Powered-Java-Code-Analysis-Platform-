
import { useEffect, useState } from "react";
import {
  getMyProjects,
  createProject,
  updateProject,
  deleteProject,
} from "../services/api";

import type {
  Project,
  CreateProjectRequest,
} from "../services/api";

interface ProjectsProps {
  setActivePage: (page: string) => void;
}

const emptyForm: CreateProjectRequest = {
  name: "",
  description: "",
  sourceType: "GITHUB",
  githubUrl: "",
};

export default function Projects({ setActivePage }: ProjectsProps) {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  const [form, setForm] = useState<CreateProjectRequest>({
    ...emptyForm,
  });

  const loadProjects = async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getMyProjects();
      setProjects(data);
    } catch {
      setError("Failed to load projects. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjects();
  }, []);

  const resetForm = () => {
    setForm({ ...emptyForm });
    setEditingId(null);
    setShowForm(false);
  };

  const handleCreateOrUpdate = async (
    e: React.FormEvent
  ) => {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");

      if (editingId !== null) {
        await updateProject(editingId, form);
      } else {
        await createProject(form);
      }

      resetForm();
      await loadProjects();
    } catch {
      setError(
        editingId !== null
          ? "Failed to update project. Please check your inputs."
          : "Failed to create project. Please check your inputs."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (project: Project) => {
    setForm({
      name: project.name,
      description: project.description || "",
      sourceType: project.sourceType,
      githubUrl: project.githubUrl || "",
    });

    setEditingId(project.id);
    setShowForm(true);
    setError("");
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Are you sure you want to delete this project?")) {
      return;
    }

    try {
      setError("");
      await deleteProject(id);
      await loadProjects();
    } catch {
      setError("Failed to delete project.");
    }
  };

  // Navigate to Dashboard with the selected GitHub repository
  const handleAnalyze = (project: Project) => {
    if (project.sourceType !== "GITHUB" || !project.githubUrl?.trim()) {
      setError("This project does not have a valid GitHub URL.");
      return;
    }

    localStorage.setItem("githubUrl", project.githubUrl);
    setActivePage("dashboard");
  };

  return (
    <div className="p-6">
      {/* Page Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold">My Projects</h1>
          <p className="text-gray-500">
            Manage your CodeCompass repositories
          </p>
        </div>

        <button
          onClick={() => {
            if (showForm) {
              resetForm();
            } else {
              resetForm();
              setShowForm(true);
            }
          }}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          + Add Project
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <p className="text-red-600 mb-4">{error}</p>
      )}

      {/* Create / Edit Form */}
      {showForm && (
        <form
          onSubmit={handleCreateOrUpdate}
          className="border rounded-lg p-5 mb-6 space-y-4 bg-white shadow-sm"
        >
          <h2 className="text-lg font-semibold">
            {editingId !== null ? "Edit Project" : "Create New Project"}
          </h2>

          <input
            className="border rounded p-2 w-full"
            placeholder="Project Name"
            required
            value={form.name}
            onChange={(e) =>
              setForm({ ...form, name: e.target.value })
            }
          />

          <textarea
            className="border rounded p-2 w-full"
            placeholder="Description (optional)"
            value={form.description}
            onChange={(e) =>
              setForm({ ...form, description: e.target.value })
            }
          />

          <select
            className="border rounded p-2 w-full"
            value={form.sourceType}
            onChange={(e) =>
              setForm({
                ...form,
                sourceType: e.target.value as "GITHUB" | "UPLOAD",
                githubUrl: "",
              })
            }
          >
            <option value="GITHUB">GitHub Repository</option>
            <option value="UPLOAD">Upload</option>
          </select>

          {form.sourceType === "GITHUB" && (
            <input
              className="border rounded p-2 w-full"
              placeholder="GitHub Repository URL"
              required
              value={form.githubUrl}
              onChange={(e) =>
                setForm({ ...form, githubUrl: e.target.value })
              }
            />
          )}

          <div className="flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-50"
            >
              {saving
                ? "Saving..."
                : editingId !== null
                ? "Update Project"
                : "Save Project"}
            </button>

            <button
              type="button"
              onClick={resetForm}
              className="border px-4 py-2 rounded hover:bg-gray-100"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Project List */}
      {loading ? (
        <p>Loading projects...</p>
      ) : projects.length === 0 ? (
        <div className="border rounded-lg p-8 text-center">
          <h2 className="text-lg font-semibold">No projects yet</h2>
          <p className="text-gray-500 mt-2">
            Add your first GitHub project to get started.
          </p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {projects.map((project) => (
            <div
              key={project.id}
              className="border rounded-lg p-5 shadow-sm bg-white"
            >
              <h2 className="text-lg font-semibold">
                {project.name}
              </h2>

              <p className="text-gray-500 mt-2">
                {project.description || "No description"}
              </p>

              <p className="text-sm mt-3">
                Source: {project.sourceType}
              </p>

              {project.githubUrl && (
                <a
                  href={project.githubUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-blue-600 text-sm break-all hover:underline"
                >
                  {project.githubUrl}
                </a>
              )}

              {/* Project Actions */}
              <div className="mt-4 flex flex-wrap gap-3">
                {project.sourceType === "GITHUB" && (
                  <button
                    onClick={() => handleAnalyze(project)}
                    className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700"
                  >
                    Analyze
                  </button>
                )}

                <button
                  onClick={() => handleEdit(project)}
                  className="text-blue-600 border border-blue-300 px-3 py-1 rounded hover:bg-blue-50"
                >
                  Edit
                </button>

                <button
                  onClick={() => handleDelete(project.id)}
                  className="text-red-600 border border-red-300 px-3 py-1 rounded hover:bg-red-50"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}