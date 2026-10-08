(() => {
    "use strict";

    const app = document.querySelector("#app");
    const toast = document.querySelector("#toast");
    const state = { page: 0, size: 10, sort: "fullName", direction: "asc" };
    let toastTimer;

    const html = (strings, ...values) => strings.reduce((result, part, index) => result + part + (values[index] ?? ""), "");
    const escapeHtml = value => String(value ?? "").replace(/[&<>'"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[char]);
    const formatDate = value => value ? new Intl.DateTimeFormat(undefined, { year: "numeric", month: "short", day: "numeric", timeZone: "UTC" }).format(new Date(`${value}T00:00:00Z`)) : "Not provided";
    const initials = name => String(name || "?").trim().split(/\s+/).slice(0, 2).map(part => part[0]).join("").toUpperCase();

    async function api(path, options = {}) {
        const response = await fetch(path, {
            ...options,
            headers: { "Accept": "application/json", ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers }
        });
        if (!response.ok) {
            let details = {};
            try { details = await response.json(); } catch (_) { /* non-JSON response */ }
            const error = new Error(details.message || `Request failed (${response.status})`);
            error.details = details;
            error.status = response.status;
            throw error;
        }
        return response.status === 204 ? null : response.json();
    }

    function showToast(message) {
        clearTimeout(toastTimer);
        toast.textContent = message;
        toast.hidden = false;
        toastTimer = setTimeout(() => { toast.hidden = true; }, 4500);
    }

    function setNavigation(section) {
        document.querySelectorAll("[data-nav]").forEach(link => {
            const active = link.dataset.nav === section;
            link.classList.toggle("active", active);
            if (active) link.setAttribute("aria-current", "page"); else link.removeAttribute("aria-current");
        });
    }

    function renderLoading(title = "Loading patient records") {
        app.innerHTML = html`<section class="page" aria-busy="true" aria-label="${escapeHtml(title)}">
            <div class="loading card"><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div></div>
        </section>`;
    }

    function renderError(message, retry) {
        app.innerHTML = html`<section class="page"><div class="card error-state" role="alert">
            <div class="empty-icon" aria-hidden="true">!</div><h1>We couldn't load this page</h1>
            <p>${escapeHtml(message)}</p><button class="button button-primary" id="retry">Try again</button>
        </div></section>`;
        document.querySelector("#retry").addEventListener("click", retry);
    }

    async function renderPatientList() {
        setNavigation("patients");
        renderLoading();
        try {
            const params = new URLSearchParams({ page: state.page, size: state.size, sort: state.sort, direction: state.direction });
            const result = await api(`/api/patients?${params}`);
            const rows = result.content.map(patient => html`
                <article class="patient-row">
                    <div class="identity"><span class="avatar" aria-hidden="true">${escapeHtml(initials(patient.fullName))}</span><div><div class="name">${escapeHtml(patient.fullName)}</div><div class="secondary">${escapeHtml(patient.medicalRecordNumber)}</div></div></div>
                    <div class="secondary">${escapeHtml(formatDate(patient.dateOfBirth))}</div>
                    <div class="secondary email">${escapeHtml(patient.email || "No email")}</div>
                    <div class="row-action"><a class="button button-secondary" href="#/patients/${encodeURIComponent(patient.medicalRecordNumber)}" aria-label="View ${escapeHtml(patient.fullName)}'s profile">View profile</a></div>
                </article>`).join("");

            app.innerHTML = html`<section class="page">
                <div class="page-heading"><div><p class="eyebrow">Patient workspace</p><h1>Patients</h1><p class="lede">Find a record. Continue their care.</p></div><a class="button button-primary" href="#/register"><span aria-hidden="true">＋</span> Register patient</a></div>
                <div class="toolbar card" aria-label="List controls">
                    <div class="field compact"><label for="sort">Sort by</label><select id="sort"><option value="fullName">Name</option><option value="medicalRecordNumber">Medical record number</option><option value="dateOfBirth">Date of birth</option><option value="createdAt">Date registered</option></select></div>
                    <div class="field compact"><label for="page-size">Rows per page</label><select id="page-size"><option>5</option><option>10</option><option>20</option><option>50</option></select></div>
                    <button class="button button-secondary sort-direction" id="direction" aria-label="Sort ${state.direction === "asc" ? "descending" : "ascending"}">${state.direction === "asc" ? "Ascending ↑" : "Descending ↓"}</button>
                </div>
                <div class="patient-list card">
                    ${result.content.length ? html`<div class="table-head" aria-hidden="true"><span>Patient</span><span>Date of birth</span><span>Email</span><span>Action</span></div>${rows}` : html`<div class="empty"><div class="empty-icon" aria-hidden="true">＋</div><h2>No patients yet</h2><p>Register the first patient to begin their care record.</p><a class="button button-primary" href="#/register">Register patient</a></div>`}
                </div>
                <div class="results-footer"><span>Showing ${result.content.length ? result.page * result.size + 1 : 0}–${Math.min((result.page + 1) * result.size, result.totalElements)} of ${result.totalElements} patients</span>
                    <div class="pagination" aria-label="Pagination"><button class="button button-secondary" id="previous" ${result.page === 0 ? "disabled" : ""}>Previous</button><span class="page-number">Page ${result.totalPages ? result.page + 1 : 0} of ${result.totalPages}</span><button class="button button-secondary" id="next" ${result.page + 1 >= result.totalPages ? "disabled" : ""}>Next</button></div>
                </div>
            </section>`;
            const sort = document.querySelector("#sort"); sort.value = state.sort;
            const size = document.querySelector("#page-size"); size.value = String(state.size);
            sort.addEventListener("change", () => { state.sort = sort.value; state.page = 0; renderPatientList(); });
            size.addEventListener("change", () => { state.size = Number(size.value); state.page = 0; renderPatientList(); });
            document.querySelector("#direction").addEventListener("click", () => { state.direction = state.direction === "asc" ? "desc" : "asc"; state.page = 0; renderPatientList(); });
            document.querySelector("#previous").addEventListener("click", () => { state.page--; renderPatientList(); });
            document.querySelector("#next").addEventListener("click", () => { state.page++; renderPatientList(); });
        } catch (error) { renderError(error.message, renderPatientList); }
    }

    function fieldError(form, field, message) {
        const input = form.elements[field];
        input.setAttribute("aria-invalid", "true");
        const target = form.querySelector(`[data-error-for="${field}"]`);
        if (target) target.textContent = message;
    }

    function clearErrors(form) {
        form.querySelectorAll("[aria-invalid]").forEach(input => input.removeAttribute("aria-invalid"));
        form.querySelectorAll(".field-error").forEach(error => { error.textContent = ""; });
        const summary = form.querySelector(".form-error");
        if (summary) { summary.hidden = true; summary.textContent = ""; }
    }

    function validatePatient(form, includeMrn) {
        clearErrors(form);
        const data = Object.fromEntries(new FormData(form));
        let valid = true;
        if (includeMrn && !data.medicalRecordNumber.trim()) { fieldError(form, "medicalRecordNumber", "Enter a medical record number."); valid = false; }
        if (!data.fullName.trim()) { fieldError(form, "fullName", "Enter the patient's full name."); valid = false; }
        if (!data.dateOfBirth) { fieldError(form, "dateOfBirth", "Enter a date of birth."); valid = false; }
        else if (data.dateOfBirth > new Date().toISOString().slice(0, 10)) { fieldError(form, "dateOfBirth", "Date of birth cannot be in the future."); valid = false; }
        if (data.email && !form.elements.email.validity.valid) { fieldError(form, "email", "Enter a valid email address."); valid = false; }
        return { valid, data };
    }

    function showFormError(form, error) {
        const summary = form.querySelector(".form-error");
        summary.textContent = error.status === 409 ? "That medical record number is already in use." : error.message;
        summary.hidden = false;
        summary.focus();
    }

    function renderRegistration() {
        setNavigation("register");
        app.innerHTML = html`<section class="page"><a class="back-link" href="#/patients">← Back to patients</a>
            <div class="page-heading"><div><p class="eyebrow">New care record</p><h1>Register a patient</h1><p class="lede">Add the details needed to begin a patient record.</p></div></div>
            <form class="card form-card" id="patient-form" novalidate>
                <div class="form-error" role="alert" tabindex="-1" hidden></div>
                <div class="form-grid">
                    <div class="field full"><label for="mrn">Medical record number</label><input id="mrn" name="medicalRecordNumber" maxlength="64" autocomplete="off" required aria-describedby="mrn-hint mrn-error"><span class="hint" id="mrn-hint">A unique identifier, for example HZN-2048.</span><span class="field-error" id="mrn-error" data-error-for="medicalRecordNumber"></span></div>
                    <div class="field full"><label for="full-name">Full name</label><input id="full-name" name="fullName" maxlength="200" autocomplete="name" required aria-describedby="name-error"><span class="field-error" id="name-error" data-error-for="fullName"></span></div>
                    <div class="field"><label for="dob">Date of birth</label><input id="dob" name="dateOfBirth" type="date" max="${new Date().toISOString().slice(0, 10)}" required aria-describedby="dob-error"><span class="field-error" id="dob-error" data-error-for="dateOfBirth"></span></div>
                    <div class="field"><label for="email">Email <span class="hint">(optional)</span></label><input id="email" name="email" type="email" maxlength="320" autocomplete="email" aria-describedby="email-error"><span class="field-error" id="email-error" data-error-for="email"></span></div>
                </div>
                <div class="form-actions"><a class="button button-secondary" href="#/patients">Cancel</a><button class="button button-primary" type="submit">Register patient</button></div>
            </form>
        </section>`;
        const form = document.querySelector("#patient-form");
        form.addEventListener("submit", async event => {
            event.preventDefault();
            const { valid, data } = validatePatient(form, true);
            if (!valid) { form.querySelector("[aria-invalid]")?.focus(); return; }
            const button = form.querySelector("[type=submit]"); button.disabled = true; button.textContent = "Registering…";
            try {
                const patient = await api("/api/patients", { method: "POST", body: JSON.stringify({ ...data, email: data.email || null }) });
                showToast(`${patient.fullName} was registered successfully.`);
                location.hash = `#/patients/${encodeURIComponent(patient.medicalRecordNumber)}`;
            } catch (error) { showFormError(form, error); button.disabled = false; button.textContent = "Register patient"; }
        });
    }

    async function renderProfile(mrn) {
        setNavigation("patients");
        renderLoading("Loading patient profile");
        try {
            const [patient, encounters] = await Promise.all([
                api(`/api/patients/${encodeURIComponent(mrn)}`),
                api(`/api/patients/${encodeURIComponent(mrn)}/encounters`)
            ]);
            app.innerHTML = profileMarkup(patient, encounters);
            bindProfile(patient);
        } catch (error) {
            if (error.status === 404) renderError("This patient record could not be found.", () => { location.hash = "#/patients"; });
            else renderError(error.message, () => renderProfile(mrn));
        }
    }

    function profileMarkup(patient, encounters) {
        const encounterItems = encounters.length ? encounters.map(encounter => html`<article class="encounter">
            <h3>${escapeHtml(encounter.type)}</h3><div class="encounter-meta">${escapeHtml(formatDate(encounter.date))}${encounter.clinician ? ` · ${escapeHtml(encounter.clinician)}` : ""}</div>
            <p class="encounter-notes">${escapeHtml(encounter.notes || "No notes recorded.")}</p></article>`).join("") : html`<div class="empty"><div class="empty-icon" aria-hidden="true">＋</div><h3>No encounters recorded</h3><p>Add this patient's first encounter when care is provided.</p></div>`;
        return html`<section class="page"><a class="back-link" href="#/patients">← Back to patients</a>
            <div class="page-heading"><div class="profile-header"><span class="avatar" aria-hidden="true">${escapeHtml(initials(patient.fullName))}</span><div><span class="badge">${escapeHtml(patient.medicalRecordNumber)}</span><h1>${escapeHtml(patient.fullName)}</h1><p class="lede">Patient profile</p></div></div></div>
            <div class="profile-grid">
                <section class="card details-card" aria-labelledby="details-title"><div class="section-heading"><h2 id="details-title">Patient details</h2><button class="button button-secondary" id="edit-patient">Edit</button></div>
                    <div id="patient-details"><dl class="details"><div><dt>Full name</dt><dd>${escapeHtml(patient.fullName)}</dd></div><div><dt>Date of birth</dt><dd>${escapeHtml(formatDate(patient.dateOfBirth))}</dd></div><div><dt>Email</dt><dd>${escapeHtml(patient.email || "Not provided")}</dd></div></dl></div>
                </section>
                <section class="card encounters-card" aria-labelledby="encounters-title"><div class="section-heading"><div><h2 id="encounters-title">Encounter history</h2><div class="secondary">${encounters.length} ${encounters.length === 1 ? "encounter" : "encounters"}</div></div><button class="button button-primary" id="add-encounter">Record encounter</button></div>
                    <div id="encounter-form-slot"></div><div class="encounter-list">${encounterItems}</div>
                </section>
            </div>
        </section>`;
    }

    function bindProfile(patient) {
        document.querySelector("#edit-patient").addEventListener("click", () => showEditForm(patient));
        document.querySelector("#add-encounter").addEventListener("click", () => showEncounterForm(patient));
    }

    function showEditForm(patient) {
        const container = document.querySelector("#patient-details");
        document.querySelector("#edit-patient").hidden = true;
        container.innerHTML = html`<form id="edit-form" novalidate><div class="form-error" role="alert" tabindex="-1" hidden></div><div class="form-grid">
            <div class="field full"><label for="edit-name">Full name</label><input id="edit-name" name="fullName" maxlength="200" value="${escapeHtml(patient.fullName)}" required><span class="field-error" data-error-for="fullName"></span></div>
            <div class="field full"><label for="edit-dob">Date of birth</label><input id="edit-dob" name="dateOfBirth" type="date" max="${new Date().toISOString().slice(0, 10)}" value="${escapeHtml(patient.dateOfBirth)}" required><span class="field-error" data-error-for="dateOfBirth"></span></div>
            <div class="field full"><label for="edit-email">Email <span class="hint">(optional)</span></label><input id="edit-email" name="email" type="email" maxlength="320" value="${escapeHtml(patient.email || "")}"><span class="field-error" data-error-for="email"></span></div>
            </div><div class="form-actions"><button class="button button-secondary" type="button" id="cancel-edit">Cancel</button><button class="button button-primary" type="submit">Save changes</button></div></form>`;
        document.querySelector("#cancel-edit").addEventListener("click", () => renderProfile(patient.medicalRecordNumber));
        const form = document.querySelector("#edit-form");
        form.addEventListener("submit", async event => {
            event.preventDefault();
            const { valid, data } = validatePatient(form, false);
            if (!valid) { form.querySelector("[aria-invalid]")?.focus(); return; }
            const button = form.querySelector("[type=submit]"); button.disabled = true; button.textContent = "Saving…";
            try {
                await api(`/api/patients/${encodeURIComponent(patient.medicalRecordNumber)}`, { method: "PUT", body: JSON.stringify({ ...data, email: data.email || null }) });
                showToast("Patient details saved."); renderProfile(patient.medicalRecordNumber);
            } catch (error) { showFormError(form, error); button.disabled = false; button.textContent = "Save changes"; }
        });
    }

    function showEncounterForm(patient) {
        const slot = document.querySelector("#encounter-form-slot");
        document.querySelector("#add-encounter").hidden = true;
        slot.innerHTML = html`<form class="encounter-form" id="encounter-form" novalidate><div class="form-error" role="alert" tabindex="-1" hidden></div><div class="form-grid">
            <div class="field"><label for="encounter-date">Date</label><input id="encounter-date" name="date" type="date" max="${new Date().toISOString().slice(0, 10)}" value="${new Date().toISOString().slice(0, 10)}" required><span class="field-error" data-error-for="date"></span></div>
            <div class="field"><label for="encounter-type">Type</label><input id="encounter-type" name="type" maxlength="100" placeholder="Annual checkup" required><span class="field-error" data-error-for="type"></span></div>
            <div class="field full"><label for="clinician">Clinician</label><input id="clinician" name="clinician" maxlength="200" autocomplete="name" required><span class="field-error" data-error-for="clinician"></span></div>
            <div class="field full"><label for="notes">Notes <span class="hint">(optional)</span></label><textarea id="notes" name="notes" maxlength="4000"></textarea><span class="field-error" data-error-for="notes"></span></div>
            </div><div class="form-actions"><button class="button button-secondary" id="cancel-encounter" type="button">Cancel</button><button class="button button-primary" type="submit">Save encounter</button></div></form>`;
        document.querySelector("#cancel-encounter").addEventListener("click", () => { slot.innerHTML = ""; document.querySelector("#add-encounter").hidden = false; });
        const form = document.querySelector("#encounter-form");
        form.querySelector("#encounter-type").focus();
        form.addEventListener("submit", async event => {
            event.preventDefault(); clearErrors(form);
            const data = Object.fromEntries(new FormData(form)); let valid = true;
            if (!data.date) { fieldError(form, "date", "Enter an encounter date."); valid = false; }
            else if (data.date > new Date().toISOString().slice(0, 10)) { fieldError(form, "date", "Encounter date cannot be in the future."); valid = false; }
            if (!data.type.trim()) { fieldError(form, "type", "Enter an encounter type."); valid = false; }
            if (!data.clinician.trim()) { fieldError(form, "clinician", "Enter the clinician's name."); valid = false; }
            if (!valid) { form.querySelector("[aria-invalid]")?.focus(); return; }
            const button = form.querySelector("[type=submit]"); button.disabled = true; button.textContent = "Saving…";
            try {
                await api(`/api/patients/${encodeURIComponent(patient.medicalRecordNumber)}/encounters`, { method: "POST", body: JSON.stringify(data) });
                showToast("Encounter recorded successfully."); renderProfile(patient.medicalRecordNumber);
            } catch (error) { showFormError(form, error); button.disabled = false; button.textContent = "Save encounter"; }
        });
    }

    function route() {
        const path = location.hash.replace(/^#/, "") || "/patients";
        const profileMatch = path.match(/^\/patients\/(.+)$/);
        if (path === "/patients" || path === "/") renderPatientList();
        else if (path === "/register") renderRegistration();
        else if (profileMatch) renderProfile(decodeURIComponent(profileMatch[1]));
        else { location.hash = "#/patients"; }
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    window.addEventListener("hashchange", route);
    route();
})();
